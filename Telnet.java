import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Telnet {
    public static void main(final String... args) throws Exception {
        try (
             final Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
             final InputStream in = socket.getInputStream();
             final OutputStream out = socket.getOutputStream();
        ) {
            final ForkJoinPool pool = ForkJoinPool.commonPool();
            pool.submit(() -> copy(System.in, out));
            pool.submit(() -> copy(in, System.out));
            pool.awaitQuiescence(Integer.MAX_VALUE, TimeUnit.SECONDS);
        }
    }

    private static void copy(final InputStream in, final OutputStream out) {
        try {
            for (int c; (c = in.read()) != -1; ) {
                if (c == 10)
                    out.write(13);
                out.write(c);
                if (c == 10)
                    out.flush();
            }
        } catch (final IOException e) {
            System.err.println(e);
        }
    }
}
