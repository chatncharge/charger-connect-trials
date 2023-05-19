import java.io.*;
import java.net.*;
import java.util.*;

public class ModbusTest {
    private static final int MODBUS_PORT = 502;
    private static final int DEFAULT_TIMEOUT_IN_MS = 1000;

    private static String getHost(final String... args) { return args.length > 0 ? args[0] : "192.168.8.9"; }
    private static int getPort(final String... args) { return args.length > 1 ? Integer.parseInt(args[1]) : MODBUS_PORT; }
    private static int getTimeoutInMilliseconds(final String... args) { return args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_TIMEOUT_IN_MS; }

    public static void main(final String... args) {
        final String host = getHost(args);
        final int port = getPort(args);
        final int timeoutInMilliseconds = getTimeoutInMilliseconds(args);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutInMilliseconds);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Modbus TCP request to read input registers
            int transactionId = 0x0001;
            int protocolId = 0x0000;
            int unitId = 0x01; // Modbus slave ID
            int functionCode = 0x04; // Read Input Registers
            int startingAddress = 0x0000;
            int quantity = 0x0002;

            // Modbus TCP request payload
            byte[] requestPayload = {
                    (byte) (transactionId >> 8), (byte) transactionId, // Transaction ID
                    (byte) (protocolId >> 8), (byte) protocolId, // Protocol ID
                    0, (byte) 6, // Message length
                    (byte) unitId, // Unit Identifier
                    (byte) functionCode, // Function Code
                    (byte) (startingAddress >> 8), (byte) startingAddress, // Starting Address
                    (byte) (quantity >> 8), (byte) quantity // Quantity of Registers
            };

            // Send Modbus TCP request
            outputStream.write(requestPayload);
            outputStream.flush();

            // Read Modbus TCP response
            byte[] responsePayload = new byte[260]; // Adjust size as per your requirements
            int bytesRead = inputStream.read(responsePayload);
            System.err.println("Read " + bytesRead + " bytes.");

            // Process the Modbus TCP response
            if (bytesRead > 0) {
                // Extract the data from the response
                int byteCount = responsePayload[8] & 0xFF;
                for (int i = 0; i < byteCount / 2; i++) {
                    int dataIndex = 9 + i * 2;
                    int msb = responsePayload[dataIndex] & 0xFF;
                    int lsb = responsePayload[dataIndex + 1] & 0xFF;
                    int registerValue = (msb << 8) | lsb;
                    System.out.println("Register " + i + ": " + registerValue);
                }
            }


        } catch (final Exception e) {
            System.err.format("Couldn't connect to %s:%d, reason: %s%n", host, port, e.getMessage());
        }
    }
}
