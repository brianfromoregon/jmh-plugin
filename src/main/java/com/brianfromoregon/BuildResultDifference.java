package com.brianfromoregon;

import com.google.caliper.ScenarioResult;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Map;

/**
 * The difference in results between two builds.
 */
public class BuildResultDifference {
    public final ImmutableList<ScenarioResultChange> changes;

    public BuildResultDifference(BuildResults old, BuildResults nu) {
        ImmutableList.Builder<ScenarioResultChange> builder = ImmutableList.builder();
        for (Map.Entry<ScenarioKey, ScenarioResult> entry : nu.getScenarios().entrySet()) {
            ScenarioResult oldResult = null;
            if (old != null) {
                oldResult = old.getScenarios().get(entry.getKey());
            }
            builder.add(new ScenarioResultChange(entry.getKey(), oldResult, entry.getValue()));
        }
        changes = builder.build();
    }

    public int getNumMemoryResults() {
        return Iterables.size(Iterables.filter(changes, new Predicate<ScenarioResultChange>() {
            @Override
            public boolean apply(ScenarioResultChange input) {
                return input.getBytesType() != ScenarioResultChange.Type.NO_RESULT || input.getInstancesType() != ScenarioResultChange.Type.NO_RESULT;
            }
        }));
    }

    public int getNumTimingResults() {
        return Iterables.size(Iterables.filter(changes, new Predicate<ScenarioResultChange>() {
            @Override
            public boolean apply(ScenarioResultChange input) {
                return input.getTimeType() != ScenarioResultChange.Type.NO_RESULT;
            }
        }));
    }

    public int getNumMemoryRegressions() {
        return Iterables.size(Iterables.filter(changes, new Predicate<ScenarioResultChange>() {
            @Override
            public boolean apply(ScenarioResultChange input) {
                return input.getBytesType() == ScenarioResultChange.Type.WORSE || input.getInstancesType() == ScenarioResultChange.Type.WORSE;
            }
        }));
    }
}
