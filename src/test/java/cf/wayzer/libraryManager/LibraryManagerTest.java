package cf.wayzer.libraryManager;

import example.KotlinMainKt;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LibraryManagerTest {
    @Test
    public void testDownload() throws Exception {
        LibraryManager inst = new LibraryManager();
        inst.addAliYunMirror();
        inst.require(Dependency.KOTLIN_RUNTIME);
        inst.load();
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
