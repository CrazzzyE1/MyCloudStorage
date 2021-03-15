package serverApp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.util.Arrays;

public class FileHandler extends SimpleChannelInboundHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Message: " + msg);
        String[] strings = ((String) msg)
                .replace("\r", "")
                .replace("\n", "")
                .trim().split(" ");
        String command = strings[0];
        System.out.println(command);
        System.out.println(Arrays.asList(strings));
        if (command.equals("list-files")) {
            File file = new File("MyServer/src/main/resources/server");
            File[] files = file.listFiles();
            StringBuffer sb = new StringBuffer();
            for (File f : files) {
                sb.append(f.getName() + " ");
            }
            ctx.writeAndFlush(sb.toString());
        } else if (command.equals("auth")) {
            ctx.writeAndFlush("authsuccess");
        } else if (command.equals("reg")) {
            ctx.writeAndFlush("regsuccess");
        } else if (command.equals("createDir")) {
            ctx.writeAndFlush("dirSuccess");
        }
//        else {
//            System.out.println("Chanel closed");
//            ctx.channel().closeFuture();
//            ctx.channel().close();
//        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }
}
