package cf.wayzer.libraryManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

class DownloadManager {
    private final Path rootDir;
    private final Logger logger;
    private static final String FILE_FORMAT = "%s-%s.jar";
    private static final String MAVEN_FORMAT = "%s%s/%s/%s/%s-%s.jar";
    private MessageDigest digest;

    DownloadManager(Path rootDir, Logger logger) throws LibraryLoadException {
        this.rootDir = rootDir;
        this.logger = logger;
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
            logger.info("Load " + file_name + " from local");
            dependency.jarFile = f.toFile();
            return;
        }

        try {
            URL url = new URL(String.format(MAVEN_FORMAT,
                    dependency.repositoryUrl,
                    dependency.group.replace(".", "/"),
                    dependency.name, dependency.version, dependency.name, dependency.version
            ));
            logger.info("Start download " + file_name + " from " + url);
            HttpURLConnection con;
            while (true) {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                if (con.getResponseCode() == 301 || con.getResponseCode() == 302) {
                    String newUrl = con.getHeaderField("Location");
                    if (newUrl == null || newUrl.isEmpty())
                        throw new LibraryLoadException("Download Fail: Empty Redirect");
                    logger.info("==> Redirect to " + newUrl);
                    url = new URL(newUrl);
                } else break;
            }
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
                logger.info("End download " + file_name);
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
