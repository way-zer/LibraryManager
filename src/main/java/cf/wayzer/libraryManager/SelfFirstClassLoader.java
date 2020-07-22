package cf.wayzer.libraryManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class SelfFirstClassLoader extends URLClassLoader {
    private final Filter filter;

    public interface Filter {
        /**
         * whether self first
         *
         * @return true to load class by self(for special usage)
         */
        boolean handle(String name);
    }

    public SelfFirstClassLoader(URL[] urls, ClassLoader parent, Filter filter) {
        super(urls, parent != null ? parent : SelfFirstClassLoader.class.getClassLoader());
        this.filter = filter;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!filter.handle(name)) return super.loadClass(name, resolve);
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                String path = name.replace('.', '/').concat(".class");
                URL res = getParent().getResource(path);
                if (res != null) {
                    try (InputStream stream = res.openStream()) {
                        byte[] bs = new byte[stream.available()];
                        return defineClass(name, bs, 0, stream.read(bs));
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Can't load Class:" + name, e);
                    }
                }
                c = findClass(name);//must exist in findClass
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t0);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
}
