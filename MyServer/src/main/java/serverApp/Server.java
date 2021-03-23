package serverApp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import serverApp.Controllers.ClientHandler;
import serverApp.Controllers.DbController;

import java.io.File;

public class Server {

    //Server settings
    private final int SERVER_PORT = 1234;
    private final String mainPath = "MyCloud";

    //Database settings
    private final String DB_HOST = "localhost";
    private final int DB_PORT = 3306;
    private final static String user = "root";
    private final static String password = "Viking07";

    private final DbController dbController;


    public Server() {
        dbController = new DbController(DB_HOST, DB_PORT, user, password);
        File folder = new File(mainPath);
        if (!folder.exists()) folder.mkdir();
    }

    public void run(){
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup workers = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, workers)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch){
                            ch.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new ClientHandler(dbController, mainPath)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(SERVER_PORT).sync();
            System.out.println("Server started");
            future.channel().closeFuture().sync();
            System.out.println("Server finished");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }
}
