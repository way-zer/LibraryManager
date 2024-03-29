package cf.wayzer.libraryManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SelfFirstClassLoader extends MutableURLClassLoader {
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
                String path = name.replace('.', '/').concat(".class");
                //search self first
                URL res = findResource(path);
                //try parent
                if (res == null) res = getResource(path);
                if (res != null) {
                    try (InputStream stream = res.openStream()) {
                        byte[] bs = readAll(stream);
                        return defineClass(name, bs, 0, bs.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Can't load Class:" + name, e);
                    }
                }
                c = findClass(name);//must exist in findClass
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    private static byte[] readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(8192, stream.available()));
        byte[] buffer = new byte[8192];
        while (true) {
            int read = stream.read(buffer);
            if (read == -1) break;
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
