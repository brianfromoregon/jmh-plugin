package com.brianfromoregon;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class Stats {
    public final SummaryStatistics stats = new SummaryStatistics(); // in nanos
    private final SubSecond unit;

    public Stats(SubSecond unit) {
        this.unit = unit;
    }

    public void addValue(double nanos) {
        stats.addValue(nanos);
    }

    public String getN() {
        return String.valueOf(stats.getN());
    }

    public String getMean() {
        return unit.format(stats.getMean());
    }

    public String getMax() {
        return unit.format(stats.getMax());
    }

    public String getMin() {
        return unit.format(stats.getMin());
    }

    public String getStandardDeviation() {
        return unit.format(stats.getStandardDeviation());
    }
}
