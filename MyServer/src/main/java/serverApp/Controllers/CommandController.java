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

    private String nameFile = "";
    private String search = "";
    private StringBuilder sb = new StringBuilder();

    public CommandController(DbController dbController, String mainPath) {
        this.dbController = dbController;
        this.mainPath = mainPath;
        this.previousPath = mainPath;
        this.rootPath = mainPath;
    }

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
            if (!folder.exists()) {
                folder.mkdir();
            }
            mainPath = mainPath.concat("/").concat(login);
            rootPath = mainPath;
            previousPath = mainPath;
            return "authsuccess";
        } else {
            return "autherror";
        }
    }

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

    public String mkdir(String[] strings) {
        File folder = new File(mainPath + File.separator + strings[1].replace("??", " "));
        if (!folder.exists()) {
            folder.mkdir();
            return "dirSuccess";
        } else {
            return "unSuccess";
        }
    }

    public String rm(String[] strings) {
        File rm = new File(mainPath + File.separator + strings[1].replace("??", " "));
        if (rm.exists()) {
            rm.delete();
            return "rmSuccess";
        } else {
            return "unSuccess";
        }
    }

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
}
