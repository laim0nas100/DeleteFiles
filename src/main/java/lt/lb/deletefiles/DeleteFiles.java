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
    
    public static void prinUsageExit(){
        printUsage();
        try{
            Thread.sleep(1000);
        }catch(InterruptedException ex){
            
        }
        
        System.exit(1);
    }

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
            System.out.println("Deleting files older than: " + days + " days at: " + dir);
            deleteStart(dir, time);
        }
        Thread.sleep(1000);

    }

    public static boolean timeBefore(Path p, Instant test) throws IOException {
        FileTime time = Files.getLastModifiedTime(p);
        if (time == null || test == null) {
            return true;
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
