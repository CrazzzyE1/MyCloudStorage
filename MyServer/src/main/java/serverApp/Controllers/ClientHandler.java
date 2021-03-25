package serverApp.Controllers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;

public class ClientHandler extends SimpleChannelInboundHandler {

    private final CommandController commandController;
    private boolean downloadFlag = false;
    private boolean uploadFlag = false;
    private long uploadFileSize = 0L;
    private long count = 0L;

    public ClientHandler(DbController dbController, String mainPath) {
        commandController = new CommandController(dbController, mainPath);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg2){
        System.out.println("_________________________________________");

        ByteBuf buffer = ((ByteBuf) msg2).copy();
        if(!uploadFlag) {
//            StringBuilder sbbb = new StringBuilder();
            byte [] bytestmp = new byte[buffer.capacity()];
            for (int i = 0; i < buffer.capacity(); i++) {
//                byte b = buffer.getByte(i);
//                sbbb.append((char) b);
//                sbbb.append(b);
                bytestmp[i] = buffer.getByte(i);
            }

//            String msg = sbbb.toString();
            String msg = new String(bytestmp, 0, bytestmp.length, StandardCharsets.UTF_8);
            String[] strings = msg
                    .replace("\r", "")
                    .replace("\n", "")
                    .trim().split(" ");
            String command = strings[0];
            System.out.println(command);

            switch (command) {
                case ("ls"):
                    msg = commandController.ls();
                    break;
                case ("auth"):
                    msg = commandController.auth(strings);
                    break;
                case ("reg"):
                    msg = commandController.reg(strings);
                    break;
                case ("mkdir"):
                    msg = commandController.mkdir(strings);
                    break;
                case ("rm"):
                    msg = commandController.rm(strings);
                    break;
                case ("cd"):
                    msg = commandController.cd(strings);
                    break;
                case ("getAddress"):
                    msg = commandController.getAddress();
                    break;
                case ("copy"):
                case ("cut"):
                    msg = commandController.copyOrCut(strings);
                    break;
                case ("paste"):
                    msg = commandController.paste();
                    break;
                case ("search"):
                    msg = commandController.search(strings);
                    break;
                case ("download"):
                    msg = commandController.download(strings);
                    break;
                case ("waiting"):
                    downloadFlag = true;
                    System.out.println("In W");
                    break;
                case ("waitingUpload"):
                    uploadFlag = true;
                    System.out.println("UploadSize: " + strings[1]);
                    uploadFileSize = Long.parseLong(strings[1]);
//                    System.out.println("In Upload size: " + strings[1]);
                    return;
                case ("upload"):
                    msg = commandController.upload(strings);
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }

            if (downloadFlag) {
                System.out.println("In Flag");
                byte[] bytes = commandController.getBytes();
                System.out.println("Size of bytes: " + bytes.length);
                buffer = Unpooled.copiedBuffer(bytes);
                downloadFlag = false;
                ctx.writeAndFlush(buffer);
                buffer.clear();
                return;
            }


            msg2 = Unpooled.copiedBuffer(msg.getBytes(StandardCharsets.UTF_8));
//        Unpooled.copiedBuffer(byte[])
//        Unpooled.wrappedBuffer(ByteBuffer)
//        Unpooled.wrappedBuffer(byte[])
            System.out.println("in sending");
            ctx.writeAndFlush(msg2);
        }

        if (uploadFlag) {
            System.out.println("In Upload flag");
            System.out.println(buffer.capacity());
            byte[] bytes = new byte[buffer.capacity()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = buffer.getByte(i);
            }
            count += buffer.capacity();
            commandController.uploadFile(bytes);
            System.out.println("count: " + count + " uploadSize " + uploadFileSize);
            if(uploadFileSize != count) return;
            uploadFlag = false;
            uploadFileSize = 0L;
            count = 0L;
            buffer.clear();
            return;
        }
        buffer.clear();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }
}
