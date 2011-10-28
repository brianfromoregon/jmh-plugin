package com.brianfromoregon;

import com.google.caliper.Environment;
import com.google.caliper.Result;
import com.google.caliper.Scenario;
import com.google.caliper.ScenarioResult;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * The collected Caliper results from a build, mapped by ScenarioKey for comparing to other BuildResults.
 */
public class BuildResults {
    private final Environment environment;
    private final Map<ScenarioKey, ScenarioResult> scenarios;

    public BuildResults(Iterable<Result> results) {
        scenarios = Maps.newHashMap();

        // Environment should be the same across all results for the same build, right?
        environment = results.iterator().next().getEnvironment();

        for (Result result : results) {
            for (Map.Entry<Scenario, ScenarioResult> entry : result.getRun().getMeasurements().entrySet()) {
                scenarios.put(new ScenarioKey(result.getRun().getBenchmarkName(), entry.getKey()), entry.getValue());
            }
        }
    }

    public Map<ScenarioKey, ScenarioResult> getScenarios() {
        return scenarios;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
