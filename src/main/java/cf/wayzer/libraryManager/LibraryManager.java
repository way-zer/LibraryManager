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

    /**
     * Use "./" as rootDir
     */
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

    /**
     * Add a repository
     *
     * @param name the name you like
     * @param url  the url prefix
     */
    public void addRepository(String name, String url) {
        repositories.put(name, url);
    }

    /**
     * Add dependency to this LibraryManager
     * @param dependency the dependency you need
     */
    public void require(Dependency dependency) {
        if (!repositories.containsKey(dependency.repository))
            throw new RuntimeException("Can't find repository,Please add first!");
        dependency.repository = repositories.get(dependency.repository);
        if (dependencies.containsKey(dependency.name))
            throw new RuntimeException("This dependency has required:" + dependency);
        dependencies.put(dependency.name, dependency);
    }

    /**
     * Load and add to SystemClassLoader
     * @see this.load()
     */
    public void loadToClasspath() throws LibraryLoadException {
        load();
        try {
            URLClassLoader system = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method f = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            f.setAccessible(true);
            for (Dependency d : dependencies.values()) {
                f.invoke(system, d.jarFile.toURI().toURL());
            }
        } catch (Exception e) {
            throw new LibraryLoadException("load to classpath fail: ", e);
        }
    }

    /**
     * Load and create an {@code URLClassLoader} including all dependencies
     * @param parent the parent classloader
     * @return Classloader including all dependencies
     * @see this.load()
     */
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

    /**
     * Download and resolve dependencies
     * @throws LibraryLoadException Any Exception in load
     */
    public void load() throws LibraryLoadException {
        DownloadManager downloadManager = new DownloadManager(rootDir);
        for (Dependency d : dependencies.values()) {
            downloadManager.download(d);
        }
    }

    /**
     * Load {@code Dependency.KOTLIN_RUNTIME}
     */
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
