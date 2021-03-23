package serverApp.Controllers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler {

    private final CommandController commandController;

    public ClientHandler(DbController dbController, String mainPath) {
        commandController = new CommandController(dbController, mainPath);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
        System.out.println("id " + ctx.channel().id());
        System.out.println("localAddress " + ctx.channel().localAddress().toString());

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg){

        String[] strings = ((String) msg)
                .replace("\r", "")
                .replace("\n", "")
                .trim().split(" ");
        String command = strings[0];

        switch (command) {
            case ("ls"):
                ctx.writeAndFlush(commandController.ls());
                break;
            case ("auth"):
                ctx.writeAndFlush(commandController.auth(strings));
                break;
            case ("reg"):
                ctx.writeAndFlush(commandController.reg(strings));
                break;
            case ("mkdir"):
                ctx.writeAndFlush(commandController.mkdir(strings));
                break;
            case ("rm"):
                ctx.writeAndFlush(commandController.rm(strings));
                break;
            case ("cd"):
                ctx.writeAndFlush(commandController.cd(strings));
                break;
            case ("getAddress"):
                ctx.writeAndFlush(commandController.getAddress());
                break;
            case ("copy"):
            case ("cut"):
                ctx.writeAndFlush(commandController.copyOrCut(strings));
                break;
            case ("paste"):
                ctx.writeAndFlush(commandController.paste());
                break;
            case ("search"):
                ctx.writeAndFlush(commandController.search(strings));
                break;
            default:
                System.out.println("Unknown command");
                break;
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }
}
