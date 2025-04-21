package lt.lb.deletefiles;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author Lemmin
 */
public class PathAttributes {
    public final Path path;
    public final BasicFileAttributes attributes;

    public PathAttributes(Path path, BasicFileAttributes attributes) {
        this.path = path;
        this.attributes = attributes;
    }
    
}
