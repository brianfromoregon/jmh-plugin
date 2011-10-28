package com.brianfromoregon;

import com.google.caliper.Environment;
import com.google.caliper.MeasurementSet;
import com.google.caliper.MeasurementType;
import com.google.caliper.ScenarioResult;

import java.util.Map;

/**
 * The difference in memory results between two scenarios.
 */
public class ScenarioMemoryResultChange {
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
        NO_RESULT;
    }

    private final ScenarioKey scenarioKey;
    private final MeasurementSet oldBytes;
    private final MeasurementSet oldInstances;
    private final MeasurementSet newBytes;
    private final MeasurementSet newInstances;

    public ScenarioMemoryResultChange(ScenarioKey scenarioKey, ScenarioResult old, ScenarioResult nu) {
        this.scenarioKey = scenarioKey;
        if (old != null) {
            oldBytes = old.getMeasurementSet(MeasurementType.MEMORY);
            oldInstances = old.getMeasurementSet(MeasurementType.INSTANCE);
        } else {
            oldBytes = oldInstances = null;
        }
        if (nu != null) {
            newBytes = nu.getMeasurementSet(MeasurementType.MEMORY);
            newInstances = nu.getMeasurementSet(MeasurementType.INSTANCE);
        } else {
            newBytes = newInstances = null;
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

    public Integer getOldBytes() {
        if (oldBytes == null) return null;
        return (int) oldBytes.meanRaw();
    }

    public Integer getOldInstances() {
        if (oldInstances == null) return null;
        return (int) oldInstances.meanRaw();
    }


    public Type getBytesType() {
        return getType(getNewBytes(), getOldBytes());
    }

    public Type getInstancesType() {
        return getType(getNewInstances(), getOldInstances());
    }

    private Type getType(Integer nu, Integer old) {
        if (nu == null) return Type.NO_RESULT;
        if (old == null) return Type.NEW_RESULT;

        if (nu > old) return Type.WORSE;
        if (nu < old) return Type.BETTER;
        return Type.SAME;
    }

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

    public String getBenchmarkName() {
        return scenarioKey.getBenchmarkName();
    }

    public Map<String, String> getScenario() {
        return scenarioKey.getScenario().getVariables();
    }
}
