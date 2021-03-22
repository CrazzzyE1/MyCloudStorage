package serverApp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class FileHandler extends SimpleChannelInboundHandler {
    private String mainPath = "MyServer/src/main/resources/server";
//    private String mainPath = "C:/";
    private String previousPath = "MyServer/src/main/resources/server";
    private String rootPath = "MyServer/src/main/resources/server";

    private boolean cutOrCopy = false; // TRUE - COPY, FALSE - CUT
    private String nameFile = "";
    private String search = "";
    private StringBuilder sb = new StringBuilder();

    private String copyOrCutPath = "";
    private ArrayList<String> superAuth = new ArrayList<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {
        System.out.println("Message: " + msg);
        String[] strings = ((String) msg)
                .replace("\r", "")
                .replace("\n", "")
                .trim().split(" ");
        String command = strings[0];
// Получение списка файлов и отправка клиенту
        if (command.equals("ls")) {
            File file = new File(mainPath);
            File[] files = file.listFiles();
            StringBuffer sb = new StringBuffer();
            for (File f : files) {
                sb.append(f.getName() + " ");
            }
            if (sb.length() < 1) sb.append("Empty");
            ctx.writeAndFlush(sb.toString());

        } else if (command.equals("auth")) {
//Заглушка авторизации
            for (int i = 0; i < superAuth.size(); i++) {
                if (superAuth.get(i).contains(((String) msg).substring(5))) {
                    ctx.writeAndFlush("authsuccess");
                    return;
                }
            }
            ctx.writeAndFlush("authError");

        } else if (command.equals("reg")) {
//Заглушка Регистрации
            superAuth.add(((String) msg).substring(4));
            ctx.writeAndFlush("regsuccess");
        } else if (command.equals("mkdir")) {
//Создание директории на сервере в открытой папке

            File folder = new File(mainPath + File.separator + strings[1]);
            System.out.println(folder.getAbsolutePath());
            if (!folder.exists()) {
                System.out.println(folder.mkdir());
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
            if (strings[1].equals("back")) {
                mainPath = previousPath;
                previousPath = getPreviousPath(mainPath);
                ctx.writeAndFlush("cdSuccess");
            } else {
                File cd = new File(mainPath + File.separator + strings[1]);
                if (cd.exists() && cd.isDirectory()) {
                    mainPath = mainPath + "/" + strings[1];
                    previousPath = getPreviousPath(mainPath);
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
        //Подготовка к копированию или вызеранию файла (запоминаем адрес файла источника)
        else if (command.equals("copy") || command.equals("cut")) {
            File copy = new File(mainPath + File.separator + strings[1]);
            if (copy.exists()) {
                if (command.equals("copy")) {
                    cutOrCopy = true;
                } else {
                    cutOrCopy = false;
                }
                nameFile = copy.getName();
                copyOrCutPath = copy.getPath();
                ctx.writeAndFlush("rmSuccess");
            } else {
                ctx.writeAndFlush("unSuccess");
            }
        }
        // Вставка файла
        else if (command.equals("paste")) {

            if (!copyOrCutPath.isEmpty()) {
                Path pathFrom = Paths.get(copyOrCutPath);
                Path pathTo = Paths.get(mainPath + File.separator + nameFile);
                try {
                    Files.copy(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);
                    if (!cutOrCopy) Files.delete(pathFrom);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            copyOrCutPath = "";
            nameFile = "";
            ctx.writeAndFlush("pasteSuccess");
        }
        // Поиск файла
        else if (command.equals("search")) {
            sb.setLength(0);
            for (int i = 1; i < strings.length; i++) {
                sb.append(strings[i]).append(" ");
            }
            search = sb.toString().trim();
            sb.setLength(0);
            Files.walkFileTree(Paths.get(rootPath), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                        if (search.equals(file.getFileName().toString()) || (file.getFileName().toString()).contains(search)) {
                            sb.append(file.getFileName().toString()
                                    .replace(" ", ",,") + "::"
                                    + file.toAbsolutePath().toString()
                                    .replace(" ", ",,") + ",,");
                            return FileVisitResult.CONTINUE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                search = sb.toString();
                if(search.isEmpty()) search = "Not Found";
            ctx.writeAndFlush(search);
        }

        else {
            System.out.println("Unknown command");
        }
    }
//Создание строки адреса для шага Back
    public String getPreviousPath(String path) {
        if (path.equals(rootPath)) return path;
        int index = -1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                index = i;
            }
        }
        path = path.substring(0, index);
        System.out.println(path);
        return path;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }
}
