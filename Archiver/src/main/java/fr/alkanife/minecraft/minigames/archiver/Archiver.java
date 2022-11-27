package fr.alkanife.minecraft.minigames.archiver;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Archiver {

    private static boolean debug = false;
    private static List<String> logs = new ArrayList<>();

    /* archiver.txt :
    DELETE
    KEEP_RECAP
    KEEP_LOGS
    KEEP_WORLD
    KEEP_CONSOLE_HISTORY
     */
    public static void main(String[] args) {
        log("---------------------------");
        log("---- Minigame Archiver ----");
        log("---------------------------");

        if (args.length > 0)
            if (args[0].equalsIgnoreCase("debug"))
                debug = true;

        debug("Debug mode");

        debug("Checking if server/ directory is not empty");
        File serverDirectory = new File("server/");
        if (serverDirectory.exists()) {
            debug("server/ directory exists");
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(serverDirectory.toPath())) {
                boolean isEmpty = !directory.iterator().hasNext();
                if (isEmpty) {
                    error("The server/ directory is empty!");
                    exit();
                    return;
                } else {
                    debug("Not empty, continuing");
                }
            } catch (IOException e) {
                error("Failed to check if server/ directory is empty");
                e.printStackTrace();
                exit();
                return;
            }
        } else {
            error("The server/ directory does not exists!");
            exit();
            return;
        }

        boolean archiveAll = false;
        boolean deleteAll = false;
        boolean keepRecap = false;
        boolean keepLogs = false;
        boolean keepWorlds = false;
        boolean keepConsoleHistory = false;

        debug("Checking for archiver.txt");
        File archiverFile = new File("server/archiver.txt");

        if (archiverFile.exists()) {
            debug("archiver.txt found");
            try {
                debug("Reading archiver.txt");
                BufferedReader bufferedReader = new BufferedReader(new FileReader(archiverFile));

                String string = bufferedReader.readLine();

                if (string == null) {
                    archiveAll = true;
                    warn("archiver.txt file has no content, archiving all content");
                } else {
                    String[] archiveArguments = string.split(",");

                    for (String argument : archiveArguments) {
                        switch (argument.toLowerCase()) {
                            case "delete":
                                deleteAll = true;
                                debug("Delete all = true (" + argument + ")");
                                break;

                            case "keep_recap":
                                keepRecap = true;
                                debug("Keeping recap = true (" + argument + ")");
                                break;

                            case "keep_logs":
                                keepLogs = true;
                                debug("Keeping logs = true (" + argument + ")");
                                break;

                            case "keep_worlds":
                                keepWorlds = true;
                                debug("Keeping worlds = true (" + argument + ")");
                                break;

                            case "keep_console_history":
                                keepConsoleHistory = true;
                                debug("Keeping console history = true (" + argument + ")");
                                break;

                            default:
                                debug("Unknown argument: " + argument);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                archiveAll = true;
                warn("Failed to read archiver.txt file found, archiving all content");
            }
        } else {
            archiveAll = true;
            warn("No archiver.txt file found, archiving all content");
        }

        if (deleteAll) {
            log("Deleting everything");
            deleteServerDirectory(serverDirectory);
            exit();
            return;
        }

        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

        String archiveDirectoryPath = "archives/" + time + "/";

        debug("Archive directory path = " + archiveDirectoryPath);

        File archiveDirectory = new File(archiveDirectoryPath);

        if (archiveAll) {
            log("Archiving everything");

            try {
                FileUtils.moveDirectory(serverDirectory, archiveDirectory);
            } catch (IOException e) {
                error("Failed to create archive directory");
                e.printStackTrace();
                exit();
                return;
            }

            exit();
            return;
        }

        if (archiveDirectory.mkdirs()) {
            log("Creating archive directory");
        } else {
            error("Failed to create archive directory");
            exit();
            return;
        }

        if (keepRecap) {
            log("Keeping recap");

            File recap = new File("server/recap.json");

            if (recap.exists()) {
                try {
                    FileUtils.moveFile(recap, new File(archiveDirectoryPath + "recap.json"));
                } catch (IOException e) {
                    warn("Failed to move recap.json to the archive directory");
                    e.printStackTrace();
                }
            } else {
                warn("No recap.json, skipping");
            }
        }

        if (keepLogs) {
            log("Keeping logs");

            File logs = new File("server/logs/");

            if (logs.exists()) {
                try {
                    FileUtils.moveDirectory(logs, new File(archiveDirectoryPath + "logs/"));
                } catch (IOException e) {
                    warn("Failed to move logs to the archive directory");
                    e.printStackTrace();
                }
            } else {
                warn("No logs, skipping");
            }
        }

        if (keepWorlds) {
            log("Keeping worlds");

            File world = new File("server/world/");
            File world_nether = new File("server/world_nether/");
            File world_the_end = new File("server/world_the_end/");

            if (world.exists()) {
                try {
                    FileUtils.moveDirectory(world, new File(archiveDirectoryPath + "world/"));
                } catch (IOException e) {
                    warn("Failed to move world/ to the archive directory");
                    e.printStackTrace();
                }
            } else {
                warn("No world, skipping");
            }

            if (world_nether.exists()) {
                try {
                    FileUtils.moveDirectory(world_nether, new File(archiveDirectoryPath + "world_nether/"));
                } catch (IOException e) {
                    warn("Failed to move world_nether/ to the archive directory");
                    e.printStackTrace();
                }
            } else {
                warn("No world_nether, skipping");
            }

            if (world_the_end.exists()) {
                try {
                    FileUtils.moveDirectory(world_the_end, new File(archiveDirectoryPath + "world_the_end/"));
                } catch (IOException e) {
                    warn("Failed to move world_the_end/ to the archive directory");
                    e.printStackTrace();
                }
            } else {
                warn("No world_the_end, skipping");
            }
        }

        if (keepConsoleHistory) {
            log("Keeping console history");

            File consoleHistory = new File("server/.console_history");

            if (consoleHistory.exists()) {
                try {
                    FileUtils.moveFile(consoleHistory, new File(archiveDirectoryPath + ".console_history"));
                } catch (IOException e) {
                    warn("Failed to move recap.json to the archive directory");
                    e.printStackTrace();
                }
            } else {
                warn("No .console_history, skipping");
            }
        }

        log("Deleting server/ directory");
        deleteServerDirectory(serverDirectory);

        log("Done! Bye~");
        exit();
    }

    private static void exit() {
        log("---------------------------");

        File logFile = new File("archiver.log");

        boolean writeLogs = false;

        debug("Checking for existence of archiver.log");
        if (logFile.exists()) {
            writeLogs = true;
        } else {
            try {
                if (logFile.createNewFile()) {
                    writeLogs = true;
                } else {
                    warn("Failed to create archiver.log");
                }
            } catch (IOException e) {
                warn("Failed to create archiver.log");
                e.printStackTrace();
            }
        }

        if (writeLogs) {
            try {
                debug("Writing logs");

                BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
                StringBuilder stringBuilder = new StringBuilder();

                for (String log : logs)
                    stringBuilder.append(log).append("\n");

                writer.write(stringBuilder.toString());
                writer.close();
            } catch (Exception e) {
                warn("Failed to write archiver.log");
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static void log(String s) {
        System.out.println("\u001B[38;5;195m" + s + "\u001B[0m");
        logs.add("(info) " + s);
    }

    private static void error(String s) {
        System.out.println("\u001B[38;5;196m" + s + "\u001B[0m");
        logs.add("(error) " + s);
    }

    private static void warn(String s) {
        System.out.println("\u001B[38;5;208m" + s + "\u001B[0m");
        logs.add("(warn) " + s);
    }

    private static void debug(String s) {
        if (debug)
            System.out.println("\u001B[38;5;243m[debug] " + s + "\u001B[0m");
        logs.add("(debug) " + s);
    }

    private static void deleteServerDirectory(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            error("Failed to delete server directory!");
            e.printStackTrace();
        }
    }

}
