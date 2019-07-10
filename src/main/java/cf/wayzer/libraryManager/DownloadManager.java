package cf.wayzer.libraryManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

class DownloadManager {
    private Path rootDir;
    private static final String FILE_FORMAT = "%s-%s.jar";
    private static final String MAVEN_FORMAT = "%s%s/%s/%s/%s";
    private MessageDigest digest;

    DownloadManager(Path rootDir) throws LibraryLoadException {
        this.rootDir = rootDir;
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new LibraryLoadException("Can't create Dir:", e);
        }
    }

    void download(Dependency dependency) throws LibraryLoadException {
        String file_name = String.format(FILE_FORMAT, dependency.name.toLowerCase(), dependency.version);
        Path f = rootDir.resolve(file_name);
        if (Files.exists(f)) {
            dependency.jarFile = f.toFile();
            return;
        }

        try {
            URL url = new URL(String.format(MAVEN_FORMAT,
                    dependency.repository,
                    dependency.group.replace(".", "/"),
                    dependency.name, dependency.version, file_name
            ));
            URLConnection con = url.openConnection();
            try (InputStream in = con.getInputStream()) {
                byte[] bs = readInputStream(in);
                if (bs.length < 1000)
                    throw new LibraryLoadException("Download Fail: can't read all! \n" + dependency);
                if (dependency.hash != null) {
                    if (digest == null) digest = MessageDigest.getInstance("SHA-256");
                    String hash = Base64.getEncoder().encodeToString(digest.digest(bs));
                    if (!dependency.hash.contentEquals(hash))
                        throw new LibraryLoadException("Downloaded file had an invalid hash.\n" +
                                dependency + "\n" +
                                "Expected: " + dependency.hash + "\n" +
                                "Actual: " + hash);
                }
                Files.write(f, bs);
                dependency.jarFile = f.toFile();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new LibraryLoadException("Download Fail:", e);
        }
    }

    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[10 * 1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
