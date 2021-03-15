package serverApp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FileHandler extends SimpleChannelInboundHandler {
    private String mainPath = "MyServer/src/main/resources/server";
    private String previousPath = "MyServer/src/main/resources/server";
    private ArrayList <String> superAuth = new ArrayList<>();

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
// Получение списка файлов и отправка клиенту
        if (command.equals("ls")) {
            File file = new File(mainPath);
            File[] files = file.listFiles();
            StringBuffer sb = new StringBuffer();
            for (File f : files) {
                sb.append(f.getName() + " ");
            }
            if(sb.length() < 1) sb.append("Empty");
            ctx.writeAndFlush(sb.toString());

        } else if (command.equals("auth")) {
//Заглушка авторизации
            System.out.println(msg);
            for (int i = 0; i < superAuth.size(); i++) {
                if(superAuth.get(i).contains(((String) msg).substring(5)))
                {ctx.writeAndFlush("authsuccess");
                return;}
            }
            ctx.writeAndFlush("authError");

        } else if (command.equals("reg")) {
//Заглушка Регистрации
            superAuth.add(((String) msg).substring(4));
            System.out.println(superAuth);
            ctx.writeAndFlush("regsuccess");
        } else if (command.equals("mkdir")) {
//Создание директории на сервере в открытой папке
            File folder = new File(mainPath + File.separator + strings[1]);
            if (!folder.exists()) {
                folder.mkdir();
                ctx.writeAndFlush("dirSuccess");
            } else {
                ctx.writeAndFlush("unSuccess");
            }

        }
//Удаление директории или файла на сервере в открытой папке
        else if (command.equals("rm")) {
            File rm = new File(mainPath + File.separator + strings[1]);
            if (rm.exists()) {
                rm.delete();
                ctx.writeAndFlush("rmSuccess");
            } else {
                ctx.writeAndFlush("unSuccess");
            }

        }
//Смена директории на сервере
        else if (command.equals("cd")) {
            if(strings[1].equals("back")) {
                mainPath = previousPath;
                ctx.writeAndFlush("cdSuccess");
            } else {
                File cd = new File(mainPath + File.separator + strings[1]);
                if (cd.exists() && cd.isDirectory()) {
                    System.out.println("Ceo");
                    previousPath = mainPath;
                    mainPath = mainPath + "/" + strings[1];
                    System.out.println(mainPath);
                    ctx.writeAndFlush("cdSuccess");
                } else {
                    ctx.writeAndFlush("unSuccess");
                }
            }
        }
        //Отправка адреса клиенту
        else if (command.equals("getAddress")) {
            ctx.writeAndFlush(mainPath);
        }
        else {
            System.out.println("Unknown command");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }
}
