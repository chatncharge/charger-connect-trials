import java.nio.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ScanForModbus {
    private static final int[] portsToProbe = { 22, 53, 80, 443, 502, 8080, 8081, 8082, 5355 };

    public static void main(final String... args) throws Throwable {
        final InetAddress localhost = args.length > 0 ? InetAddress.getByName(args[0]) : InetAddress.getLocalHost();
        System.out.println("Localhost: " + localhost);
        final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);
        System.out.println("Network Interface: " + networkInterface);
        final List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
        System.out.println("Interface Addresses: " + interfaceAddresses);
        final ForkJoinPool pool = new ForkJoinPool(1024);
        pool.submit(() -> {
            try {
                getAllIPAddresses(interfaceAddresses.get(0)).stream().parallel().forEach(address -> {
                    //System.err.println("Probing: " + address);
                    for (int port : portsToProbe)
                        if (probePort(address, port))
                            System.out.println("Address " + address + ", port: " + port);
                });
            } catch (final UnknownHostException ignore) {
            }
        });
        pool.awaitQuiescence(1000, TimeUnit.SECONDS);
    }

    public static boolean probePort(final InetAddress address, final int port) {
        try (final Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), 500);
            return true;
        } catch (final Exception ignore) {
            return false;
        }
    }

    public static List<InetAddress> getAllIPAddresses(InterfaceAddress interfaceAddress) throws UnknownHostException {
        ByteBuffer cauldron = ByteBuffer.allocate(4);
        cauldron.put(interfaceAddress.getAddress().getAddress()); cauldron.rewind();
        int localAddress = cauldron.getInt(); cauldron.rewind();
        int netmask = -1 << (32 - interfaceAddress.getNetworkPrefixLength());
        int networkAddress = localAddress & netmask;
        int broadcastAddress = localAddress | ~netmask;
        byte[] addressBytes = new byte[4];

        List<InetAddress> addresses = new ArrayList<>();
        for (int address = networkAddress + 1; address < broadcastAddress; address++) {
            cauldron.putInt(address); cauldron.rewind();
            cauldron.get(addressBytes); cauldron.rewind();
            addresses.add(InetAddress.getByAddress(addressBytes));
        }
        return addresses;
    }
}
