package lt.lb.deletefiles;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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
public class DirStream implements FileVisitor<Path>, Iterable<Path> {

    private static final Set<FileVisitOption> emptyOptions = new HashSet<>();
    private List<Path> visited = new ArrayList<>();
    private int counter;

    public DirStream(Path path) throws IOException {
        Files.walkFileTree(Objects.requireNonNull(path), emptyOptions, 1, this);
    }

    public boolean counterMatchVisited() {
        return counter == visited.size();
    }

    public void incrementCounter() {
        counter++;
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
