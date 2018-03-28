import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private ServerSocketChannel socketchan;
    private Selector selector;
    private ByteBuffer buff = ByteBuffer.allocate(1024);
    private Charset charset = Charset.forName("UTF-8");

    public Server(String _host, int _port) {
        InetSocketAddress serverAddress = new InetSocketAddress(_host, _port);
        try {
            socketchan = ServerSocketChannel.open();
            socketchan.configureBlocking(false);
            socketchan.bind(serverAddress);
            selector = Selector.open();
            socketchan.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Server running.");
        serviceConnections();
    }

    public void serviceConnections(){
        boolean serverIsRunning = true;

        while (serverIsRunning){
            try {
                selector.select();
                Set keys = selector.keys();
                Iterator iter = keys.iterator();

                while (iter.hasNext()){
                    SelectionKey  key = (SelectionKey) iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        SocketChannel sc = socketchan.accept();
                        sc.configureBlocking(false);
                        continue;
                    }
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        serviceRequest(sc);
                        continue;
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void serviceRequest(SocketChannel sc) {
        if (!sc.isOpen()) return;

        try {
            sc.read(buff);

            String request = String.valueOf(charset.decode(buff));
            String[] instructions = request.split(" ");
            instructions[0].toLowerCase();

            if (instructions.equals("echo")) {
                System.out.println(request.substring(5));
            }
            else if (instructions.equals("calculate") || instructions.equals("calc")) {
                double result = 0;
                switch (instructions[2].charAt(0)) {
                    case '+':
                        result = Double.parseDouble(instructions[1]) + Double.parseDouble(instructions[3]);
                        break;
                    case '-':
                        result = Double.parseDouble(instructions[1]) - Double.parseDouble(instructions[3]);
                        break;
                    case '*':
                        result = Double.parseDouble(instructions[1]) * Double.parseDouble(instructions[3]);
                        break;
                    case '/':
                        result = Double.parseDouble(instructions[1]) / Double.parseDouble(instructions[3]);
                        break;
                }

                System.out.println(instructions[1]+" "+instructions[2]+" "+instructions[3]+" = "+result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(args[0], Integer.parseInt(args[1]));
    }
}
