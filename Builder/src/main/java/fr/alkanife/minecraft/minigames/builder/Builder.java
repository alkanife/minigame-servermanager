package fr.alkanife.minecraft.minigames.builder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Builder {

    private static boolean debug = false;
    private static List<String> templates = new ArrayList<>();

    public static void main(String[] args) {
        log("--------------------------");
        log("---- Minigame Builder ----");
        log("---- CTRL+C to abort  ----");
        log("--------------------------");

        if (args.length > 0)
            if (args[0].equalsIgnoreCase("debug"))
                debug = true;

        debug("Debug mode");

        debug("Looking for built.txt file");
        File builtFile = new File("built.txt");

        if (builtFile.exists()) {
            debug("File exists, deleting");
            if (!builtFile.delete()) {
                error("Failed to delete old built.txt file");
                exit();
                return;
            }
        }

        debug("Checking if server/ directory is empty");
        File serverDirectory = new File("server/");
        if (serverDirectory.exists()) {
            debug("server/ directory exists");
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(serverDirectory.toPath())) {
                boolean isEmpty = !directory.iterator().hasNext();
                if (isEmpty) {
                    debug("Empty directory, continuing");
                } else {
                    error("The server/ directory is not empty!");
                    exit();
                    return;
                }
            } catch (IOException e) {
                error("Failed to check if server/ directory is empty");
                e.printStackTrace();
                exit();
                return;
            }
        } else {
            log("The server/ directory does not exists! Creating one");
            if (!serverDirectory.mkdir()) {
                error("Failed to create the server/ directory");
                exit();
                return;
            }
        }

        debug("Loading templates");

        try (Stream<Path> paths = Files.walk(Paths.get("templates/"))) {
            paths.filter(Files::isDirectory).forEach(path -> {
                String pathString = path.toString().replaceAll("templates/", "");
                String[] subFiles = pathString.split("/");
                String template = subFiles[0];

                if (!template.equals("templates") && !templates.contains(template))
                    templates.add(template);
            });
        } catch (IOException e) {
            error("Failed to load templates!");
            e.printStackTrace();
            exit();
            return;
        }

        debug(templates.size() + " templates found");

        log(" ");
        int i = 0;
        for (String template : templates) {
            System.out.println("\u001B[38;5;191m [" + i + "] " + template + "\u001B[0m");
            i++;
        }
        log(" ");

        int selected = getTemplateSelection();

        String selectedTemplate = templates.get(selected);

        System.out.print("\u001B[38;5;195mYou have selected '\u001B[38;5;191m" + selectedTemplate + "\u001B[38;5;195m', continue? (enter)\u001B[0m");
        System.console().readLine();

        log("Copying template content into the server/ directory...");

        File templateDirectory = new File("templates/" + selectedTemplate);

        debug("Checking if template directory exists");
        if (!templateDirectory.exists()) {
            error("Failed to get the template directory");
            exit();
            return;
        }

        debug("Checking if template directory is a directory");
        if (!templateDirectory.isDirectory()) {
            error("The template directory is not a directory?");
            exit();
            return;
        }

        try {
            debug("Copying...");
            FileUtils.copyDirectory(templateDirectory, serverDirectory);
        } catch (IOException e) {
            error("Failed to copy template content!");
            e.printStackTrace();
            exit();
            return;
        }


        boolean builtCreated = false;

        try {
            debug("Creating built.txt");
            builtCreated = builtFile.createNewFile();
            debug("Writing built.txt");
            FileWriter writer = new FileWriter(builtFile);
            writer.write("Last build: " + selectedTemplate);
            writer.close();
        } catch (IOException ignored) { }

        if (!builtCreated)
            warn("Failed to write built.txt file, deleting the server directory manually may be required next time");

        log("Done! Bye~");
        exit();
    }

    private static void exit() {
        log("--------------------------");
        System.exit(0);
    }

    private static void log(String s) {
        System.out.println("\u001B[38;5;195m" + s + "\u001B[0m");
    }

    private static void error(String s) {
        System.out.println("\u001B[38;5;196m" + s + "\u001B[0m");
    }

    private static void warn(String s) {
        System.out.println("\u001B[38;5;208m" + s + "\u001B[0m");
    }

    private static void debug(String s) {
        if (debug)
            System.out.println("\u001B[38;5;243m[debug] " + s + "\u001B[0m");
    }

    private static int getTemplateSelection() {
        System.out.print("\u001B[38;5;195mSelect a template (number between 0 and " + (templates.size()-1) + "): \u001B[38;5;191m");
        Scanner scanner = new Scanner(System.in);
        String scanned = scanner.next();
        int selected;

        try {
            selected = Integer.parseInt(scanned);
        } catch (Exception e) {
            error("'" + scanned + "' is not a valid number!");
            return getTemplateSelection();
        }

        if (selected < 0 || selected > (templates.size()-1)) {
            error("'" + scanned + "' is too large!");
            return getTemplateSelection();
        }

        return selected;
    }

}
