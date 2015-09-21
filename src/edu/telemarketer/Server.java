package edu.telemarketer;

import edu.telemarketer.http.responses.Response;
import edu.telemarketer.services.ServiceRegistry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {
    public static final int TIMEOUT = 500;
    public static final int READ_CAPACITY = 4096;
    public static final int DEFAULT_PORT = 8080;
    private Logger logger = Logger.getLogger("Server");
    private InetAddress ip;
    private int port;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Selector selector;

    public Server(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length < 1 || !args[0].equals("start")) {
            System.out.println("Usage: start [address:port]");
            System.exit(1);
        }
        InetAddress ip = null;
        int port = 0;
        try {
            if (args.length == 2 && args[1].matches(".+:\\d+")) {
                String[] address = args[1].split(":");
                ip = InetAddress.getByName(address[0]);
                port = Integer.valueOf(address[1]);
            } else {
                ip = InetAddress.getLocalHost();
                port = DEFAULT_PORT;
                System.out.println("未指定地址和端口,使用默认ip和端口..." + ip.getHostAddress() + ":" + port);
            }
        } catch (UnknownHostException e) {
            System.out.println("请输入正确的ip");
            System.exit(1);
        }

        Server server = new Server(ip, port);
        server.start();
    }

    public void start() {
        init();
        while (true) {
            try {
                if (selector.select(TIMEOUT) == 0) {
                    continue;
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, () -> "selector错误");
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                try {
                    iterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        Response response = (Response) key.attachment();
                        if (response == null) {
                            continue;
                        }
                        ByteBuffer byteBuffer = response.getByteBuffer();
                        if (byteBuffer.hasRemaining()) {
                            client.write(byteBuffer);
                        }

                        if (!byteBuffer.hasRemaining()) {
                            key.cancel();
                            client.close();
                        }
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(READ_CAPACITY);
                        client.read(buffer);
                        executor.execute(new Controller(buffer, client, selector));
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e, () -> "socket channel 出错了");
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private void init() {
        System.out.println("初始化中...");
        ServerSocketChannel serverChannel;
        try {
            ServiceRegistry.registerServices();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(this.ip, this.port));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "初始化错误");
            System.exit(1);
        }
        System.out.println("服务器启动 http://" + ip.getHostAddress() + ":" + port + "/");
    }

}
