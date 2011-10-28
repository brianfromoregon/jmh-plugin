package com.brianfromoregon;

import com.google.caliper.Scenario;

/**
 * This is the key used to compare results across builds.
 */
public class ScenarioKey {
    private final String benchmarkName;
    private final Scenario scenario;

    public ScenarioKey(String benchmarkName, Scenario scenario) {
        this.benchmarkName = benchmarkName;
        this.scenario = scenario;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScenarioKey that = (ScenarioKey) o;

        if (benchmarkName != null ? !benchmarkName.equals(that.benchmarkName) : that.benchmarkName != null)
            return false;
        if (scenario != null ? !scenario.equals(that.scenario) : that.scenario != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = benchmarkName != null ? benchmarkName.hashCode() : 0;
        result = 31 * result + (scenario != null ? scenario.hashCode() : 0);
        return result;
    }
}
