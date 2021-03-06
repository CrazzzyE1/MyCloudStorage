package serverApp.Controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CommandController {
    private final DbController dbController;
    private String mainPath;
    private String previousPath;
    private String rootPath;

    private boolean cutOrCopy = false; // TRUE - COPY, FALSE - CUT
    private String copyOrCutPath = "";

    File download;
    File upload;
    private String nameFile = "";
    private String search = "";
    private StringBuilder sb = new StringBuilder();
    long sizeSpace = 0;

    public CommandController(DbController dbController, String mainPath) {
        this.dbController = dbController;
        this.mainPath = mainPath;
        this.previousPath = mainPath;
        this.rootPath = mainPath;
    }

    // Получение предыдущего путя
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

    // Список файлов на сервере
    public String ls() {
        File file = new File(mainPath);
        File[] files = file.listFiles();
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            sb.append(f.getName().replace(" ", "??")).append(" ");
        }
        if (sb.length() < 1) sb.append("Empty");
        return sb.toString();
    }


    public String auth(String[] strings) {
        String login = strings[1].trim().replace("??", " ");
        String password = strings[2].trim().replace("??", " ");
        if (dbController.auth(login, password)) {
            File folder = new File("MyCloud/" + login);
            File recycleBin = new File("MyCloud/" + login + "/!Recycle_Bin");
            if (!folder.exists()) {
                folder.mkdir();
            }
            if (!recycleBin.exists()) {
                recycleBin.mkdir();
            }
            mainPath = mainPath.concat("/").concat(login);
            rootPath = mainPath;
            previousPath = mainPath;
            Integer space = dbController.getSpace(login);
            return "authsuccess ".concat(space.toString());
        } else {
            return "autherror 0";
        }
    }

    // Регистрация
    public String reg(String[] strings) {
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
            return "regsuccess";
        } else {
            return "regerror";
        }
    }

    // Создание директории
    public String mkdir(String[] strings) {
        File folder = new File(mainPath + File.separator + strings[1].replace("??", " "));
        if (!folder.exists()) {
            folder.mkdir();
            return "dirSuccess";
        } else {
            return "unSuccess";
        }
    }

    // Удаление файла или директории
    public String rm(String[] strings) {
        File rm = new File(mainPath + File.separator + strings[1].replace("??", " "));
        if (rm.exists() && rm.isDirectory()) {
            rm.delete();
            return "rmSuccess";
        }
        if (rm.isFile()) {
            try {
                Files.copy(rm.toPath(), Paths.get(rootPath + File.separator
                        + "!Recycle_Bin" + File.separator + rm.getName()), StandardCopyOption.REPLACE_EXISTING);
                rm.delete();
                return "rmSuccess";
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "rmSuccess";
        }

        return "unSuccess";

    }

    // Смена директории
    public String cd(String[] strings) {
        if (strings[1].contains("::")) {
            mainPath = strings[1].split("::")[1]
                    .replace("??", " ")
                    .replace("\\", "/");
            System.out.println(mainPath);
            previousPath = getPreviousPath(mainPath);
            return "cdSuccess";
        }
        if (strings[1].equals("back")) {
            mainPath = previousPath;
            previousPath = getPreviousPath(mainPath);
            return "cdSuccess";
        } else {
            File cd = new File(mainPath + File.separator + strings[1].replace("??", " "));
            if (cd.exists() && cd.isDirectory()) {
                mainPath = mainPath + "/" + strings[1].replace("??", " ");
                previousPath = getPreviousPath(mainPath);
                return "cdSuccess";
            } else {
                return "unSuccess";
            }
        }
    }

    public String getAddress() {
        return mainPath;
    }

    // Копирование и Вырезание файла на сервере
    public String copyOrCut(String[] strings) {
        File copy = new File(mainPath + File.separator + strings[1].replace("??", " "));
        if (copy.exists()) {
            cutOrCopy = strings[0].equals("copy");
            nameFile = copy.getName();
            copyOrCutPath = copy.getPath();
            return "rmSuccess";
        } else {
            return "unSuccess";
        }
    }

    // Вставка файла
    public String paste() {
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
        return "pasteSuccess";
    }

    // Поиск файла на сервере в папке пользователя
    public String search(String[] strings) {
        sb.setLength(0);
        for (int i = 1; i < strings.length; i++) {
            sb.append(strings[i]).append(" ");
        }
        search = sb.toString().trim();
        sb.setLength(0);
        try {
            Files.walkFileTree(Paths.get(rootPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (search.equals(file.getFileName().toString())
                            || (file.getFileName().toString()).contains(search)) {

                        sb.append(file.getFileName().toString().replace(" ", "??")
                                .concat("::")
                                .concat(file.getParent().toString().replace(" ", "??")
                                        .concat(":: ")));
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("walkFileTree Exception");
        }
        search = sb.toString();
        if (search.isEmpty()) search = "Not Found";
        return search;
    }

    // Скачать
    public String download(String[] strings) {
        download = new File(mainPath + File.separator + strings[1].replace("??", " "));
        if (download.exists() && !download.isDirectory()) {
            return "downloadSuccess " + download.length();
        } else {
            return "unSuccess";
        }
    }


    // Загрузка в облако
    public String upload(String[] strings) {
        upload = new File(mainPath + File.separator + strings[1].replace("??", " "));
        if (upload.exists()) {
            nameFile = "copy_".concat(strings[1]);
        } else {
            nameFile = strings[1];
        }
        return "uploadSuccess";
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[512];
        try {
            bytes = Files.readAllBytes(download.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }


    public void uploadFile(byte[] bytes) {
        nameFile = nameFile.replace("??", " ");
        if (!Files.exists(Paths.get(mainPath + "/" + nameFile))) {
            try {
                Files.createFile(Paths.get(mainPath + "/" + nameFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.write(Paths.get(mainPath + "/" + nameFile), bytes, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String checkSpace() {
        sizeSpace = 0;
        try {
            Files.walkFileTree(Paths.get(rootPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    sizeSpace += file.toFile().length();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("walkFileTree Exception");
        }

        return String.valueOf(sizeSpace);
    }

    public String recycleClean() {
        try {
            Files.walkFileTree(Paths.get(rootPath + File.separator + "!Recycle_Bin"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    file.toFile().delete();

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("walkFileTree Exception");
        }
        return "recycleCleanSuccess";
    }

    public String restore() {
        try {
            Files.walkFileTree(Paths.get(rootPath + File.separator + "!Recycle_Bin"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        Path path = file.toFile().toPath();
                        Files.copy(path, Paths.get(rootPath + File.separator + file.toFile().getName()), StandardCopyOption.REPLACE_EXISTING);
                        Files.delete(file.toFile().toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("walkFileTree Exception");
        }
        return "recycleCleanSuccess";
    }

    public String changePassword(String[] strings) {
        String login = strings[1];
        String oldPass = strings[2];
        String newPass = strings[3];
        boolean res = dbController.changePass(login, oldPass, newPass);

        if(res) {
            return "success";
        } else  {
            return "updateError";
        }
    }

    public String remove(String[] strings) {
        String login = strings[1];
        String oldPass = strings[2];

        boolean res = dbController.removeAccount(login, oldPass);
        if(res) {
            File folder = new File(rootPath.replace("/", "\\"));
            File folder2 = new File(rootPath.replace("/", "\\").concat("_remove"));
            if(folder.exists()){
                folder.renameTo(folder2);
            }
            return "success";
        } else  {
            return "updateError";
        }
    }
}
