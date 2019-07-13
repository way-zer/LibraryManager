package cf.wayzer.libraryManager;

import java.io.File;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Dependency implements Cloneable {
    public static Dependency KOTLIN_RUNTIME = new Dependency("org.jetbrains.kotlin", "kotlin-stdlib", "1.3.41");
    public String repository;
    public String group;
    public String name;
    public String version;
    /**
     * sha256, set if you want check
     */
    public String hash;
    File jarFile;

    Dependency(String str) {
        this(str, LibraryManager.MAVEN_CENTRAL);
    }

    /**
     * @param str        gradle style dependency
     * @param repository the name you add in LibraryManager
     */
    Dependency(String str, String repository) {
        this(str.split(":")[0], str.split(":")[1], str.split(":")[2], repository);
    }

    Dependency(String group, String name, String version) {
        this(group, name, version, LibraryManager.MAVEN_CENTRAL);
    }

    Dependency(String group, String name, String version, String repository) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.repository = repository;
    }

    @Override
    public Dependency clone() {
        try {
            return (Dependency) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "repository='" + repository + '\'' +
                ", group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", hash='" + hash + '\'' +
                ", jarFile=" + jarFile +
                '}';
    }
}
