package chill.util;

import chill.utils.TheMissingUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TheMissingUtilsTest {

    @Test
    void testLazyString() {
        final String[] s = {null};
        Object obj = TheMissingUtils.lazyStr(() -> {
            s[0] = "foo";
            return s[0];
        });
        assertNull(s[0]);
        assertEquals("foo", obj.toString());
        assertEquals("foo", s[0]);
    }


}
