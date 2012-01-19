package com.brianfromoregon;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSubSecond {

    @Test
    public void testFormatTime() {
        assertEquals("1.0ns", SubSecond.finestFor(1).format(1));
        assertEquals("100.0ns", SubSecond.finestFor(100).format(100));
        assertEquals("1.0μs", SubSecond.finestFor(1001).format(1001));
        assertEquals("1.1μs", SubSecond.finestFor(1099).format(1099));
        assertEquals("999.1μs", SubSecond.finestFor(999100).format(999100));
        assertEquals("1.1ms", SubSecond.finestFor(1100000).format(1100000));
        assertEquals("999.1ms", SubSecond.finestFor(999099999).format(999099999));
        assertEquals("1.1s", SubSecond.finestFor(1100000000).format(1100000000));
        assertEquals("999.1s", SubSecond.finestFor(999100000000L).format(999100000000L));
        assertEquals("1001.1s", SubSecond.finestFor(1001100000000L).format(1001100000000L));
    }
}
