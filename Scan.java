import java.io.*;
import java.net.*;

public class Scan {
    public static void main(final String... args) {
        String ip;
        if (args.length > 0)
            ip = args[0];
        else
            ip = "192.168.8.9";
        for (int i = 0; i < 65535; i++) {
            int j = i;
            new Thread(() -> {
                try {
                    //System.out.println("Trying " + j);
                    var socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, j), 1000);
                    System.out.println("Port " + j + " connected");
                } catch (Exception e) {
                }
            }).start();
        }
    }
}
