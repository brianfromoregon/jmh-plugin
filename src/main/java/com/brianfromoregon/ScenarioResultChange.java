package com.brianfromoregon;

import com.google.caliper.MeasurementSet;
import com.google.caliper.MeasurementType;
import com.google.caliper.ScenarioResult;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * The difference in memory results between two scenarios.
 */
public class ScenarioResultChange {

    public static enum Type {
        // New is better than old
        BETTER,
        // New is worse than old
        WORSE,
        // New and old are same
        SAME,
        // There is a new value but no old value
        NEW_RESULT,
        // There is no new value
        NO_RESULT
    }

    private final ScenarioKey scenarioKey;
    private final MeasurementSet oldBytes;
    private final MeasurementSet oldInstances;
    private final MeasurementSet oldTime;
    private final MeasurementSet newBytes;
    private final MeasurementSet newInstances;
    private final MeasurementSet newTime;

    public ScenarioResultChange(ScenarioKey scenarioKey, ScenarioResult old, ScenarioResult nu) {
        this.scenarioKey = scenarioKey;
        if (old != null) {
            oldBytes = old.getMeasurementSet(MeasurementType.MEMORY);
            oldInstances = old.getMeasurementSet(MeasurementType.INSTANCE);
            oldTime = old.getMeasurementSet(MeasurementType.TIME);
        } else {
            oldBytes = oldInstances = oldTime = null;
        }
        if (nu != null) {
            newBytes = nu.getMeasurementSet(MeasurementType.MEMORY);
            newInstances = nu.getMeasurementSet(MeasurementType.INSTANCE);
            newTime = nu.getMeasurementSet(MeasurementType.TIME);
        } else {
            newBytes = newInstances = newTime = null;
        }
    }

    public Integer getNewBytes() {
        if (newBytes == null) return null;
        return (int) newBytes.meanRaw();
    }

    public Integer getNewInstances() {
        if (newInstances == null) return null;
        return (int) newInstances.meanRaw();
    }

    public Double getNewNanos() {
        if (newTime == null) return null;
        return newTime.meanRaw();
    }

    public Integer getOldBytes() {
        if (oldBytes == null) return null;
        return (int) oldBytes.meanRaw();
    }

    public Integer getOldInstances() {
        if (oldInstances == null) return null;
        return (int) oldInstances.meanRaw();
    }

    public Double getOldNanos() {
        if (oldTime == null) return null;
        return oldTime.meanRaw();
    }

    public Type getBytesType() {
        return getType(getNewBytes(), getOldBytes());
    }

    public Type getInstancesType() {
        return getType(getNewInstances(), getOldInstances());
    }

    public Type getTimeType() {
        return getType(getNewNanos(), getOldNanos());
    }

    private Type getType(Number nu, Number old) {
        if (nu == null) return Type.NO_RESULT;
        if (old == null) return Type.NEW_RESULT;

        double dnu = nu.doubleValue(), dold = old.doubleValue();
        if (Math.abs(dnu - dold) < 1) return Type.SAME;
        if (dnu > dold) return Type.WORSE;
        if (dnu < dold) return Type.BETTER;
        return Type.SAME;
    }

    /**
     * For CaliperBuildAction/index.jelly
     */
    public String getBytesString() {
        switch (getBytesType()) {
            case NO_RESULT:
                return "";
            case NEW_RESULT:
            case SAME:
                return getNewBytes().toString();
            default:
                return getNewBytes() + " (was " + getOldBytes() + ")";
        }
    }

    /**
     * For CaliperBuildAction/index.jelly
     */
    public String getInstancesString() {
        switch (getInstancesType()) {
            case NO_RESULT:
                return "";
            case NEW_RESULT:
            case SAME:
                return getNewInstances().toString();
            default:
                return getNewInstances() + " (was " + getOldInstances() + ")";
        }
    }

    /**
     * For CaliperBuildAction/index.jelly
     */
    public String getTimeString() {
        switch (getTimeType()) {
            case NO_RESULT:
                return "";
            case NEW_RESULT:
            case SAME:
                return formatTime(getNewNanos());
            default:
                double pct = Math.abs(getNewNanos() / getOldNanos() - 1);
                String direction;
                if (pct > 0)
                    direction = "up";
                else
                    direction = "down";
                return String.format("%s (%s %s%%)", formatTime(getNewNanos()), direction, new DecimalFormat("0.0").format(pct * 100));
        }
    }

    /**
     * For CaliperBuildAction/index.jelly
     */
    public String getBenchmarkName() {
        return scenarioKey.getBenchmarkName();
    }

    /**
     * For CaliperBuildAction/index.jelly
     */
    public Map<String, String> getScenario() {
        return scenarioKey.getScenario().getVariables();
    }

    static String formatTime(double nanos) {
        double step = nanos;
        int n = 0;
        for (; step >= 1000 && n < 3; n++)
            step /= 1000d;

        String unit;
        switch (n) {
            case 0:
                unit = "ns";
                break;
            case 1:
                unit = "μs";
                break;
            case 2:
                unit = "ms";
                break;
            default:
                unit = "s";
                break;
        }
        return new DecimalFormat("0.0").format(step) + unit;
    }
}
