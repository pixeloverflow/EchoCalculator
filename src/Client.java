import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class Client {
    private SocketChannel socketChannel;
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");
    private String request;
    private boolean isRunning;

    public Client(String _host, int _port, String _request) {
        InetSocketAddress socketAddress = new InetSocketAddress(_host, _port);
        try {
            socketChannel = SocketChannel.open(socketAddress);
            socketChannel.configureBlocking(false);
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        request = _request;
        System.out.println("Connected.");
        serviceConnection();
    }

    private void serviceConnection() {
        isRunning = true;

        while (isRunning) {
            try {
                selector.select();
                Set keys = selector.keys();
                Iterator iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = (SelectionKey) iter.next();
                    iter.remove();

                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        sendRequest(sc);
                    }
                }
                } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendRequest(SocketChannel sc) {
        if (!sc.isOpen()) return;

        try {
            ByteBuffer buffer = charset.encode(request);
            sc.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRunning = false;
        System.out.println("Sent : "+request);
    }

    public static void main(String[] args) {
        String request = "";
        for (int i = 2; i <= args.length-1 ;++i) {
            request += args[i]+" ";
        }

        Client client = new Client(args[0], Integer.parseInt(args[1]), request);
    }
}
