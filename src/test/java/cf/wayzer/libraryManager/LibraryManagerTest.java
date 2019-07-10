package cf.wayzer.libraryManager;

import example.KotlinMainKt;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LibraryManagerTest {
    @Test
    public void testKotlin() {
        assertFalse(hasKotlin());
        LibraryManager.loadKotlinStd();
        assertTrue(hasKotlin());
    }

    @Test
    public void testMain() {
        assertFalse(hasKotlin());
        KotlinMainKt.main();
        assertTrue(hasKotlin());
    }

    private boolean hasKotlin() {
        try {
            return Class.forName("kotlin.Lazy") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}