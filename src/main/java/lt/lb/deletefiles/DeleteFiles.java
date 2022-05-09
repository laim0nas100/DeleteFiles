package lt.lb.deletefiles;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author laim0nas100
 */
public class DeleteFiles {

    static boolean DEBUG = false;

    public static void printUsage() {
        System.out.println("Expecting 2 arguments, [days:int] [dir:string]");
        System.out.println("days: how old the last modification must be to be allowed for deletion");
        System.out.println("dir: directory to scan recursively for file deletion");
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            printUsage();
        }

        int days = 30;
        Path dir = null;
        if (args.length >= 1) {
            try {
                days = Integer.parseInt(args[0]);
                dir = Paths.get(args[1]);
            } catch (Exception ex) {
                printUsage();
                System.out.println("Error:" + ex.getMessage());
                return;
            }
        }

        System.out.println("Deleting files older than: " + days + " days at: " + dir);

        deleteStart(dir, ZonedDateTime.now()
                .minusDays(days)
                .toInstant()
        );

    }

    public static boolean timeBefore(Path p, Instant test) throws IOException {
        FileTime time = Files.getLastModifiedTime(p);
        if (time == null || test == null) {
            return true;
        }
        return time.toInstant().isBefore(test);
    }

    public static class DirStream implements FileVisitor<Path>, Iterable<Path> {

        private static final Set<FileVisitOption> emptyOptions = new HashSet<>();
        private List<Path> visited = new ArrayList<>();
        private int deleted;

        public DirStream(Path path) throws IOException {
            Files.walkFileTree(Objects.requireNonNull(path), emptyOptions, 1, this);
        }

        public boolean deletedMatchVisited() {
            return deleted == visited.size();
        }

        public void incrementDeleted() {
            deleted++;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            visited.add(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public Iterator<Path> iterator() {
            return visited.iterator();
        }

    }

    public static boolean doDelete(Path p) throws IOException {
        boolean deleted = false;
        if (!DEBUG) {
            deleted = Files.deleteIfExists(p);
        }
        System.out.println(p);
        return DEBUG || deleted;
    }

    public static void deleteStart(Path p, Instant maxTime) throws IOException {
        if (!Files.isDirectory(p)) {// file or link
            throw new IOException(p + " is not a directory");
        }

        for (Path path : new DirStream(p)) {
            delete(path, maxTime);
        }

    }

    public static boolean delete(Path p, Instant maxTime) throws IOException {
        boolean deleted = false;
        if (p == null) {
            return deleted;
        }
        if (!Files.isDirectory(p)) {// file or link
            if (timeBefore(p, maxTime)) {
                deleted = doDelete(p);
            }
            return deleted;
        }

        DirStream dirStream = new DirStream(p);

        for (Path path : dirStream) {
            if (delete(path, maxTime)) {
                dirStream.incrementDeleted();
            }
        }
        if (dirStream.deletedMatchVisited() && timeBefore(p, maxTime)) {
            deleted = doDelete(p);
        }

        return deleted;

    }
}
