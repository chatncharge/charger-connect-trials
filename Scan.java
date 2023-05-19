import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class Scan {
    public static void main(final String... args) {
        String ip;
        if (args.length > 0)
            ip = args[0];
        else
            ip = "192.168.8.9";
        final ForkJoinPool pool = new ForkJoinPool(2048);
        pool.submit(() -> {
            IntStream.range(1, 65536).parallel().forEach(port -> {
                try {
                    var socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), 1000);
                    System.out.println("Port " + port + " connected");
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            });
        });
        pool.awaitQuiescence(1000, TimeUnit.SECONDS);
    }
}
