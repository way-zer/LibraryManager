package cf.wayzer.libraryManager;

import example.KotlinMainKt;
import org.junit.Test;

import static org.junit.Assert.*;

public class LibraryManagerTest {
    @Test
    public void testDownload() throws Exception {
        LibraryManager inst = new LibraryManager();
        inst.addAliYunMirror();
        inst.require(Dependency.KOTLIN_RUNTIME);
        inst.load();
    }

    @Test
    public void testSelfFirstClassLoader() throws Exception {
        LibraryManager inst = new LibraryManager();
        Class<?> childClass = inst.createSelfFirstClassloader(null, name ->
                name.startsWith("cf.wayzer")
        ).loadClass("cf.wayzer.libraryManager.LibraryManager");
        assertNotSame(LibraryManager.class, childClass);
    }

    //Must be last(this will change SystemClassLoader)
    @Test
    public void testMain() {
        assertFalse(hasKotlin());
        KotlinMainKt.main();
        assertTrue(hasKotlin());
    }

    private boolean hasKotlin() {
        try {
            Class.forName("kotlin.Lazy");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
