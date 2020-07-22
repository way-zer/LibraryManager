package cf.wayzer.libraryManager;

import java.io.File;

@SuppressWarnings({"WeakerAccess"})
public class Dependency implements Cloneable {
    public final static Dependency KOTLIN_RUNTIME = new Dependency("org.jetbrains.kotlin", "kotlin-stdlib", "1.3.41");
    public String repository;
    public String group;
    public String name;
    public String version;
    /**
     * sha256, set if you want check
     */
    public String hash;

    String repositoryUrl;
    File jarFile;

    public Dependency(String str) {
        this(str, Repository.DEFAULT);
    }

    /**
     * @param str        gradle style dependency
     * @param repository the name you add in LibraryManager Or repositoryUrl
     */
    public Dependency(String str, String repository) {
        this(str.split(":")[0], str.split(":")[1], str.split(":")[2], repository);
    }

    public Dependency(String group, String name, String version) {
        this(group, name, version, Repository.DEFAULT);
    }

    public Dependency(String group, String name, String version, String repository) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.repository = repository;
    }

    public String getRepositoryUrl() {
        if (repositoryUrl == null)
            throw new Error("LibraryManager.require first");
        return repositoryUrl;
    }

    public File getJarFile() {
        if (jarFile == null)
            throw new Error("LibraryManager.require first");
        return jarFile;
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
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", jarFile=" + jarFile +
                '}';
    }
}
