

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import lt.lb.deletefiles.DeleteFiles;

/**
 *
 * @author laim0nas100
 */
public class DeleteTest {
    public static void main(String[] args) throws IOException{
        DeleteFiles.DEBUG = false;
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
        DeleteFiles.markingStart(dir, ZonedDateTime.now().minusDays(10).toInstant());
        DeleteFiles.deleteStart(dir);
    }
}
