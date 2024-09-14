package lt.lb.deletefiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author laim0nas100
 */
public class DeleteFiles {

    public static boolean DEBUG = false;

    private static final String SUFFIX = "_2BREMOVED";

    public static void prinUsageExit() {
        printUsage();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {

        }

        System.exit(1);
    }

    public static long totalMarked = 0;
    public static long totalDeleted = 0;

    public static void printUsage() {
        System.out.println("Expecting 2 (or more) arguments, [days:int] [dirs:string]...");
        System.out.println("days: how old the last modification must be to be allowed for deletion");
        System.out.println("dirs: directories to scan recursively for file deletion");

    }

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            prinUsageExit();
            return;
        }

        int days = 30;
        List<Path> dirs = new ArrayList<>();
        if (args.length > 1) {
            try {
                days = Integer.parseInt(args[0]);
                for (int i = 1; i < args.length; i++) {
                    dirs.add(Paths.get(args[i]));
                }
            } catch (Exception ex) {
                System.out.println("Error:" + ex.getMessage());
                prinUsageExit();
            }
        }

        if (dirs.isEmpty()) {
            prinUsageExit();
        }
        Instant time = ZonedDateTime.now()
                .minusDays(days)
                .toInstant();

        for (Path dir : dirs) {
            System.out.println("Deleting marked files at: " + dir);
            deleteStart(dir);
        }

        for (Path dir : dirs) {
            System.out.println("Marking files " + SUFFIX + " older than: " + days + " days at: " + dir);
            markForDeletion(dir, time);
        }
        System.out.println("Deleted:" + totalDeleted + " Marked:" + totalMarked);
        System.out.print("Exiting");
        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
            System.out.print(".");
        }

    }

    public static boolean timeBefore(Path p, Instant test) throws IOException {
        FileTime time = Files.getLastModifiedTime(p);
        if (time == null || test == null) {
            return false;
        }
        return time.toInstant().isBefore(test);
    }

    public static boolean doDelete(Path p) {
        boolean deleted = false;
        if (!DEBUG) {
            try {
                Files.delete(p);
                deleted = true;
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
        if (DEBUG || deleted) {
            System.out.println(p);
            return true;
        }
        return false;
    }

    public static Path getFreePathForRename(Path p) {

        String pathName = p.getName(p.getNameCount() - 1).toString();
        int counter = 0;
        String name = SUFFIX;
        while (true) {

            Path sibling = p.resolveSibling(pathName + name);
            if (!Files.exists(sibling)) {
                return sibling;
            }
            counter++;
            name = counter + SUFFIX;
        }
    }

    public static boolean doMark(Path p) {
        boolean marked = false;
        Path path = null;
        try {
            path = getFreePathForRename(p);
            if (!DEBUG) {
                Files.move(p, path);
            }
            marked = true;
        } catch (Exception ex) {
            System.err.println(ex);
        }
        if (DEBUG || marked) {
            System.out.println(p + " -> " + path);
        }
        return marked;
    }

    public static void deleteStart(Path p) throws IOException {
        if (!Files.isDirectory(p)) {// file or link
            throw new IOException(p + " is not a directory");
        }

        for (Path path : new DirStream(p)) {
            DeleteFiles.delete(path);
        }

    }

    public static void markingStart(Path p, Instant time) throws IOException {
        if (!Files.isDirectory(p)) {// file or link
            throw new IOException(p + " is not a directory");
        }

        for (Path path : new DirStream(p)) {
            DeleteFiles.markForDeletion(path, time);
        }

    }

    public static boolean delete(Path p) throws IOException {
        boolean deleted = false;
        if (p == null) {
            return deleted;
        }

        boolean suffixMatch = p.toString().endsWith(SUFFIX);

        if (!Files.isDirectory(p)) {// file or link

            if (suffixMatch) {
                deleted = doDelete(p);
                if (deleted) {
                    totalDeleted++;
                }
                return deleted;
            }
            return deleted;
        }

        // is directory
        DirStream dirStream = new DirStream(p);

        for (Path path : dirStream) {
            if (delete(path)) {
                dirStream.incrementCounter();
            }
        }
        if (suffixMatch && dirStream.counterMatchVisited()) {
            deleted = doDelete(p);
            if (deleted) {
                totalDeleted++;
            }
        }

        return deleted;
    }

    public static boolean markForDeletion(Path p, Instant maxTime) throws IOException {
        boolean marked = false;
        if (p == null) {
            return false;
        }
        if (p.toString().endsWith(SUFFIX)) {//allready marked
            return true;
        }
        if (!Files.isDirectory(p)) {// file or link
            if (timeBefore(p, maxTime)) {
                marked = doMark(p);
                if (marked) {
                    totalMarked++;
                }
            }
            return marked;
        }

        DirStream dirStream = new DirStream(p);

        for (Path path : dirStream) {
            if (DeleteFiles.markForDeletion(path, maxTime)) {
                dirStream.incrementCounter();
            }
        }
        if (dirStream.counterMatchVisited() && timeBefore(p, maxTime)) {
            marked = doMark(p);
            if (marked) {
                totalMarked++;
            }
        }

        return marked;

    }
}
