package com.brianfromoregon;

import com.google.caliper.Json;
import com.google.caliper.Result;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: is this slave friendly?
 */
public class CaliperBuildAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(CaliperBuildAction.class.getName());
    private final String[] jsonResults;
    public final AbstractBuild<?, ?> build;

    private transient BuildResults results;

    public CaliperBuildAction(String[] jsonResults, AbstractBuild<?, ?> build) {
        this.jsonResults = jsonResults;
        this.build = build;

        initResults();
    }

    /**
     * For CaliperBuildAction/index.jelly
     */
    public Iterable<ScenarioResultChange> getScenarioResultChanges() {
        return Iterables.filter(getResultDifference().changes, new Predicate<ScenarioResultChange>() {
            @Override
            public boolean apply(ScenarioResultChange input) {
                return input.getTimeType() != ScenarioResultChange.Type.NO_RESULT;
            }
        });
    }

    /**
     * For CaliperBuildAction/summary.jelly
     */
    public String getSummary() {
        BuildResultDifference diff = getResultDifference();
        int memResults = diff.getNumMemoryResults();
        int memRegressions = diff.getNumMemoryRegressions();
        int timingResults = diff.getNumTimingResults();

        return String.format("Collected %d timing result%s, %d memory result%s, found %d regression%s",
                timingResults,
                timingResults == 1 ? "" : "s",
                memResults,
                memResults == 1 ? "" : "s",
                memRegressions,
                memRegressions == 1 ? "" : "s");
    }

    public BuildResultDifference getResultDifference() {
        BuildResults prevResults = null;
        {
            AbstractBuild<?, ?> prevBuild = this.build.getPreviousBuiltBuild();
            if (prevBuild != null) {
                CaliperBuildAction prevAction = prevBuild.getAction(CaliperBuildAction.class);
                if (prevAction != null) {
                    prevResults = prevAction.results;
                }
            }
        }

        return new BuildResultDifference(prevResults, results);
    }

    @Override
    public String getIconFileName() {
        return "/plugin/caliper-ci/caliper.png";
    }

    @Override
    public String getDisplayName() {
        return "Caliper";
    }

    @Override
    public String getUrlName() {
        return "caliper";
    }

    private void initResults() {
        List<Result> caliperResults = Lists.newArrayList();
        for (String r : jsonResults) {
            try {
                caliperResults.add(Json.getGsonInstance().fromJson(r, Result.class));
            } catch (JsonSyntaxException e) {
                LOGGER.log(Level.SEVERE, "Could not parse Caliper result file as JSON, skipping", e);
            }
        }
        results = new BuildResults(caliperResults);
    }

    private Object readResolve() {
        initResults();
        return this;
    }
}