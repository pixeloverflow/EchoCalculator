import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Client {
    private SocketChannel socketChannel;
    private Selector selector;
    private String request;
    private boolean isRunning;

    private Client(String _host, int _port, String _request) {
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
        sendRequest(socketChannel);
        while (isRunning) {
            try {
                selector.select();
                Set keys = selector.selectedKeys();
                Iterator iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = (SelectionKey) iter.next();
                    iter.remove();

                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        receive(sc);
                    }
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void receive(SocketChannel sc){
        ByteBuffer buff = ByteBuffer.allocate(1024);
        try {
            sc.read(buff);
            String ans = new String(buff.array(), UTF_8);
            ans = ans.split(" ")[0];
            System.out.println("Answer received is "+ans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(SocketChannel sc) {
        if (!sc.isOpen()) return;
        try {
            ByteBuffer buffer = UTF_8.encode(request);
            sc.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
