import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class OCPPExample {
    public static void main(String[] args) {
        // Charger connection details
        String chargerIP = args[0];
        int chargerPort = 80;

        // Charger identification details
        String chargerSerialNumber = "CHARGER_SERIAL_NUMBER";
        String connectorId = "CONNECTOR_ID"; // Adjust this to match your connector ID

        try {
            // Establish a TCP socket connection with the charger
            Socket socket = new Socket(chargerIP, chargerPort);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Create an OCPP request to authorize and start a transaction
            String uniqueId = "YOUR_UNIQUE_ID";
            String authorizeRequest = createAuthorizeRequest(uniqueId, chargerSerialNumber);
            String startTransactionRequest = createStartTransactionRequest(uniqueId, connectorId);

            // Send the authorization request and receive the response
            String authorizeResponse = sendRequestTcp(authorizeRequest, inputStream, outputStream);
            System.out.println("Authorize response: " + authorizeResponse);

            // Send the start transaction request and receive the response
            String startTransactionResponse = sendRequestTcp(startTransactionRequest, inputStream, outputStream);
            System.out.println("Start transaction response: " + startTransactionResponse);

            // Create an OCPP request to retrieve meter values
            String meterValuesRequest = createMeterValuesRequest(connectorId);

            // Send the meter values request and receive the response
            String meterValuesResponse = sendRequestTcp(meterValuesRequest, inputStream, outputStream);
            System.out.println("Meter values response: " + meterValuesResponse);

            // Parse the meter values response and extract the energy consumption information
            double totalKWh = parseMeterValuesResponse(meterValuesResponse);

            // Display the total kWh consumed
            System.out.println("Total kWh consumed: " + totalKWh);

            // Close the socket connection
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createAuthorizeRequest(String uniqueId, String chargerSerialNumber) {
        // Create an OCPP Authorize request JSON string
        return "{\"chargePointSerialNumber\":\"" + chargerSerialNumber + "\",\"idTag\":\"" + uniqueId + "\",\"action\":\"Authorize\"}";
    }

    private static String createStartTransactionRequest(String uniqueId, String connectorId) {
        // Create an OCPP StartTransaction request JSON string
        return "{\"connectorId\":\"" + connectorId + "\",\"idTag\":\"" + uniqueId + "\",\"action\":\"StartTransaction\"}";
    }

    private static String createMeterValuesRequest(String connectorId) {
        // Create an OCPP MeterValues request JSON string
        return "{\"connectorId\":\"" + connectorId + "\",\"action\":\"MeterValues\"}";
    }

    private static String sendRequestTcp(String request, InputStream inputStream, OutputStream outputStream) throws IOException {
        // Convert the request string to bytes
        byte[] requestData = request.getBytes(StandardCharsets.UTF_8);

        // Send the request to the charger
        outputStream.write(requestData);
        outputStream.flush();

        // Read the response from the charger
        StringBuilder responseBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }

        return responseBuilder.toString();
    }

    private static String sendRequestHttp(String request, String chargerUrl) throws IOException {
        URL url = new URL(chargerUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the HTTP connection
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        // Send the request
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] requestData = request.getBytes(StandardCharsets.UTF_8);
            outputStream.write(requestData);
            outputStream.flush();
        }

        // Read the response
        StringBuilder responseBuilder = new StringBuilder();
        try (InputStream inputStream = connection.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        return responseBuilder.toString();
    }
    private static double parseMeterValuesResponse(String meterValuesResponse) {
        // Parse the meter values response and extract the energy consumption information
        // Adjust the parsing logic based on the structure of the response JSON
        // Here's a simple example assuming the response contains a "meterValue" field with energy consumption
        double totalKWh = 0;

        try {
            // Parse the JSON response
            // Assuming the response contains a "meterValue" field with energy consumption
            // Adjust the parsing logic based on the actual response structure
            String meterValueField = "\"meterValue\":";
            int startIndex = meterValuesResponse.indexOf(meterValueField);
            if (startIndex != -1) {
                startIndex += meterValueField.length();
                int endIndex = meterValuesResponse.indexOf("}", startIndex);
                String meterValueJson = meterValuesResponse.substring(startIndex, endIndex);

                // Extract the energy consumption value
                String energyField = "\"energy\":";
                int energyIndex = meterValueJson.indexOf(energyField);
                if (energyIndex != -1) {
                    energyIndex += energyField.length();
                    int energyEndIndex = meterValueJson.indexOf(",", energyIndex);
                    String energyValue = meterValueJson.substring(energyIndex, energyEndIndex);
                    totalKWh = Double.parseDouble(energyValue);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return totalKWh;
    }
}
