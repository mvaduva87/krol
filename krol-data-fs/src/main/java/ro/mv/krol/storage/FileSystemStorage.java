package ro.mv.krol.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ro.mv.krol.util.Args;
import ro.mv.krol.storage.path.PathTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.*;

/**
 * Created by mihai.vaduva on 13/08/2016.
 */
@Singleton
public class FileSystemStorage implements Storage {

    public static final String CONST_ROOT_DIR_PATH = "fs.rootDir";
    private final File rootDir;
    private final PathTemplate pathTemplate;

    @Inject
    public FileSystemStorage(@Named(CONST_ROOT_DIR_PATH) String rootDirPath,
                             PathTemplate pathTemplate) {
        this.pathTemplate = Args.notNull(pathTemplate, "pathTemplate");
        this.rootDir = new File(Args.notEmpty(rootDirPath, CONST_ROOT_DIR_PATH));
        //noinspection ResultOfMethodCallIgnored
        rootDir.mkdirs();
    }

    @Override
    public String write(StorageKey key, InputStream source) throws IOException {
        File file = prepareFileFor(key);
        try (OutputStream dest = new BufferedOutputStream(new FileOutputStream(file))) {
            IOUtils.copy(source, dest);
        }
        return file.getPath();
    }

    private File prepareFileFor(StorageKey key) throws IOException {
        String relativePath = pathTemplate.getPathFor(key);
        File file = new File(rootDir.getPath() + File.separatorChar + relativePath);
        FileUtils.forceMkdirParent(file);
        return file;
    }
}
