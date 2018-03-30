import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Server {
    private ServerSocketChannel socketchan;
    private Selector selector;
    private ByteBuffer buff = ByteBuffer.allocate(1024);
    private boolean serverIsRunning = true;

    private Server(String _host, int _port) {
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

    private void serviceConnections(){

        while (serverIsRunning){
            try {
                selector.select();
                Set keys = selector.selectedKeys();
                Iterator iter = keys.iterator();
                while (iter.hasNext()){
                    SelectionKey key = (SelectionKey) iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        SocketChannel sc = socketchan.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
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
        System.exit(0);
    }

    private void serviceRequest(SocketChannel sc) {
        try {
            sc.read(buff);
            String req = new String(buff.array(), UTF_8);

            String[] instructions = req.split(" ");

            instructions[0].toLowerCase();

            if (instructions[0].equals("echo")) {
                String toPrint = "";
                for (int i = 1; i<=instructions.length-2 ;++i){
                    toPrint += instructions[i] +" ";
                }
                System.out.println(toPrint);
            }
            else if (instructions[0].equals("calculate") || instructions[0].equals("calc")) {
                double result = 0;
                double one = Double.parseDouble(instructions[1]);
                double two = Double.parseDouble(instructions[3]);
                switch (instructions[2].charAt(0)) {
                    case '+':
                        result = one + two;
                        break;
                    case '-':
                        result = one - two;
                        break;
                    case '*':
                        result = one * two;
                        break;
                    case '/':
                        result = one / two;
                        break;
                }
                System.out.println(instructions[1]+" "+instructions[2]+" "+instructions[3]+" = "+result);
                buff.clear();
                buff = StandardCharsets.UTF_8.encode(result+" ");
                sc.write(buff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(args[0], Integer.parseInt(args[1]));
    }
}
