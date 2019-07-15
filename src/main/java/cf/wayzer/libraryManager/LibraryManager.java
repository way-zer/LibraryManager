package cf.wayzer.libraryManager;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LibraryManager {
    private Path rootDir;
    private HashMap<String, String> repositories = new HashMap<>();
    private HashMap<String, Dependency> dependencies = new HashMap<>();
    private Logger logger = Logger.getLogger("LibraryManager");

    /**
     * Use "./" as rootDir
     */
    public LibraryManager() {
        this(Paths.get("./"));
    }

    public LibraryManager(Path rootDir) {
        this.rootDir = rootDir;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void addMavenCentral() {
        addRepository(Repository.MAVEN_CENTRAL, Repository.PREFIX_MAVEN_CENTRAL);
    }

    public void addJCenter() {
        addRepository(Repository.JCENTER, Repository.PREFIX_JCENTER);
    }

    /**
     * Add a repository
     *
     * @param name the name you like
     * @param url  the url prefix
     */
    public void addRepository(String name, String url) {
        String env = System.getProperty("repository");
        if (env != null && !env.isEmpty()) {
            logger.info("Set Repository.DEFAULT to " + env);
            repositories.put(Repository.DEFAULT, env);
        }
        if (repositories.isEmpty())
            repositories.put(Repository.DEFAULT, url);
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
        logger.info("Start load dependencies,please be patient");
        DownloadManager downloadManager = new DownloadManager(rootDir, logger);
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
