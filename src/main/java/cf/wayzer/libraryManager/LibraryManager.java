package cf.wayzer.libraryManager;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LibraryManager {
    public final static String MAVEN_CENTRAL = "mavenCentral";
    public final static String JCENTER = "jcenter";

    private Path rootDir;
    private HashMap<String, String> repositories = new HashMap<>();
    private HashMap<String, Dependency> dependencies = new HashMap<>();

    public LibraryManager() {
        this(Paths.get("./"));
    }

    public LibraryManager(Path rootDir) {
        this.rootDir = rootDir;
    }

    public void addMavenCentral() {
        addRepository(MAVEN_CENTRAL, "https://repo1.maven.org/maven2/");
    }

    public void addJCenter() {
        addRepository(JCENTER, "https://jcenter.bintray.com/");
    }

    public void addRepository(String name, String url) {
        repositories.put(name, url);
    }

    public void require(Dependency dependency) {
        if (!repositories.containsKey(dependency.repository))
            throw new RuntimeException("Can't find repository,Please add first!");
        dependency.repository = repositories.get(dependency.repository);
        if (dependencies.containsKey(dependency.name))
            throw new RuntimeException("This dependency has required:" + dependency);
        dependencies.put(dependency.name, dependency);
    }

    public void loadToClasspath() throws LibraryLoadException {
        load();
        try {
            URLClassLoader system = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method f = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            f.setAccessible(true);
            for (Dependency d : dependencies.values()) {
                if (!d.loaded) {
                    f.invoke(system, d.jarFile.toURI().toURL());
                    d.loaded = true;
                }
            }
        } catch (Exception e) {
            throw new LibraryLoadException("load to classpath fail: ", e);
        }
    }

    public ClassLoader getClassloader(ClassLoader parent) throws LibraryLoadException {
        load();
        URL[] urls = dependencies.values().stream().map((d) -> {
            try {
                return d.jarFile.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);
        return new URLClassLoader(urls, parent);
    }

    public void load() throws LibraryLoadException {
        DownloadManager downloadManager = new DownloadManager(rootDir);
        for (Dependency d : dependencies.values()) {
            downloadManager.download(d);
        }
    }

    public static void loadKotlinStd() {
        LibraryManager libraryManager = new LibraryManager();
        libraryManager.addMavenCentral();
        libraryManager.require(Dependency.KOTLIN_RUNTIME);
        try {
            libraryManager.loadToClasspath();
        } catch (LibraryLoadException e) {
            e.printStackTrace();
        }
    }
}
