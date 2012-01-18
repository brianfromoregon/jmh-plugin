package com.brianfromoregon;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestScenarioResultChange {


    @Test
    public void testFormatTime() {
        assertEquals("1.0ns", ScenarioResultChange.formatTime(1));
        assertEquals("100.0ns", ScenarioResultChange.formatTime(100));
        assertEquals("1.0μs", ScenarioResultChange.formatTime(1001));
        assertEquals("1.1μs", ScenarioResultChange.formatTime(1099));
        assertEquals("999.1μs", ScenarioResultChange.formatTime(999100));
        assertEquals("1.1ms", ScenarioResultChange.formatTime(1100000));
        assertEquals("999.1ms", ScenarioResultChange.formatTime(999099999));
        assertEquals("1.1s", ScenarioResultChange.formatTime(1100000000));
        assertEquals("999.1s", ScenarioResultChange.formatTime(999100000000L));
        assertEquals("1001.1s", ScenarioResultChange.formatTime(1001100000000L));
    }
}
