package serverApp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileHandler extends SimpleChannelInboundHandler {

    private DbController dbController;
    private String mainPath;
    private String previousPath;
    private String rootPath;

    private boolean cutOrCopy = false; // TRUE - COPY, FALSE - CUT
    private String copyOrCutPath = "";

    private String nameFile = "";
    private String search = "";
    private StringBuilder sb = new StringBuilder();

    public FileHandler(DbController dbController, String mainPath) {
        this.dbController = dbController;
        this.mainPath = mainPath;
        this.previousPath = mainPath;
        this.rootPath = mainPath;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
        System.out.println("id " + ctx.channel().id());
        System.out.println("localAddress " + ctx.channel().localAddress().toString());

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws IOException {

        String[] strings = ((String) msg)
                .replace("\r", "")
                .replace("\n", "")
                .trim().split(" ");
        String command = strings[0];
//Получение списка файлов и отправка клиенту
        if (command.equals("ls")) {
            File file = new File(mainPath);
            File[] files = file.listFiles();
            StringBuffer sb = new StringBuffer();
            for (File f : files) {
                sb.append(f.getName().replace(" ", "??") + " ");
            }
            if (sb.length() < 1) sb.append("Empty");
            ctx.writeAndFlush(sb.toString());
//Авторизации
        } else if (command.equals("auth")) {
            String login = strings[1].trim().replace("??", " ");
            String password = strings[2].trim().replace("??", " ");
            if (dbController.auth(login, password)) {
                File folder = new File("MyCloud/" + login);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                mainPath = mainPath.concat("/").concat(login);
                rootPath = mainPath;
                previousPath = mainPath;
                ctx.writeAndFlush("authsuccess");
            } else {
                ctx.writeAndFlush("autherror");
            }
//Регистрация
        } else if (command.equals("reg")) {
            String login = strings[1].replace("??", " ");
            String password = strings[2].replace("??", " ");
            String nick = strings[3].replace("??", " ");
            boolean reg = dbController.reg(login, password, nick);
            System.out.println(reg);
            if (reg) {
                File folder = new File("MyCloud/" + login);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                ctx.writeAndFlush("regsuccess");
            } else {
                ctx.writeAndFlush("regerror");
            }
        }
//Создание директории на сервере в открытой папке
         else if (command.equals("mkdir")) {
            File folder = new File(mainPath + File.separator + strings[1].replace("??", " "));
            if (!folder.exists()) {
                folder.mkdir();
                ctx.writeAndFlush("dirSuccess");
            } else {
                ctx.writeAndFlush("unSuccess");
            }

        }
//Удаление директории или файла на сервере в открытой папке
        else if (command.equals("rm")) {
            File rm = new File(mainPath + File.separator + strings[1].replace("??", " "));
            if (rm.exists()) {
                rm.delete();
                ctx.writeAndFlush("rmSuccess");
            } else {
                ctx.writeAndFlush("unSuccess");
            }
        }
//Смена директории на сервере
        else if (command.equals("cd")) {
            if (strings[1].contains("::")) {
                mainPath = strings[1].split("::")[1]
                        .replace("??", " ")
                        .replace("\\", "/");
                System.out.println(mainPath);
                previousPath = getPreviousPath(mainPath);
                ctx.writeAndFlush("cdSuccess");
                return;

            }
            if (strings[1].equals("back")) {
                mainPath = previousPath;
                previousPath = getPreviousPath(mainPath);
                ctx.writeAndFlush("cdSuccess");
            } else {
                File cd = new File(mainPath + File.separator + strings[1].replace("??", " "));
                if (cd.exists() && cd.isDirectory()) {
                    mainPath = mainPath + "/" + strings[1].replace("??", " ");
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
            File copy = new File(mainPath + File.separator + strings[1].replace("??", " "));
            if (copy.exists()) {
                cutOrCopy = command.equals("copy");
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
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (search.equals(file.getFileName().toString()) || (file.getFileName().toString()).contains(search)) {

                        sb.append(file.getFileName().toString()
                                .replace(" ", "??") + "::"
                                + file.getParent().toString()
                                .replace(" ", "??") + ":: ");
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            search = sb.toString();
            if (search.isEmpty()) search = "Not Found";
            ctx.writeAndFlush(search);
        } else {
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
        return path;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }
}
