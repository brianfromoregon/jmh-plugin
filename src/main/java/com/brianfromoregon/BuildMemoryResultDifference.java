package com.brianfromoregon;

import com.google.caliper.ScenarioResult;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * The difference in memory results between two builds.
 */
public class BuildMemoryResultDifference {
    private final List<ScenarioMemoryResultChange> changes;

    public BuildMemoryResultDifference(BuildResults old, BuildResults nu) {
        changes = Lists.newArrayList();
        for (Map.Entry<ScenarioKey, ScenarioResult> entry : nu.getScenarios().entrySet()) {
            ScenarioResult oldResult = null;
            if (old != null) {
                oldResult = old.getScenarios().get(entry.getKey());
            }
            changes.add(new ScenarioMemoryResultChange(entry.getKey(), oldResult, entry.getValue()));
        }
    }

    public Iterable<ScenarioMemoryResultChange> getChanges(final EnumSet<ScenarioMemoryResultChange.Type> types) {
        return Iterables.filter(changes, new Predicate<ScenarioMemoryResultChange>() {
            @Override
            public boolean apply(@Nullable ScenarioMemoryResultChange input) {
                return types.contains(input.getBytesType()) || types.contains(input.getInstancesType());
            }
        });
    }

    public String getSummary() {
        int results = Iterables.size(getChanges(EnumSet.complementOf(EnumSet.of(ScenarioMemoryResultChange.Type.NO_RESULT))));
        int regressions = Iterables.size(getChanges(EnumSet.of(ScenarioMemoryResultChange.Type.WORSE)));

        return String.format("Collected %d memory result%s, found %d regression%s", results, results == 1 ? "" : "s", regressions, regressions == 1 ? "" : "s");
    }
}
