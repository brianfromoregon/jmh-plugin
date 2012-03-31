package com.brianfromoregon;

import com.google.caliper.Json;
import com.google.caliper.Result;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.Graph;
import org.kohsuke.stapler.Stapler;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaliperBuildAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(CaliperBuildAction.class.getName());
    private final String[] jsonResults;
    public final AbstractBuild<?, ?> build;

    private transient BuildResults results;
    private transient Map<Integer, ScenarioTrend> trendsById;

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

    public Iterable<ScenarioTrend> getScenarioTrends() {
        return trendsById.values();
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

    public List<Result> jsonToResults() {
        List<Result> caliperResults = Lists.newArrayList();
        for (String r : jsonResults) {
            try {
                caliperResults.add(Json.getGsonInstance().fromJson(r, Result.class));
            } catch (JsonSyntaxException e) {
                LOGGER.log(Level.SEVERE, "Could not parse Caliper result file as JSON, skipping", e);
            }
        }
        return caliperResults;
    }

    public BuildResults getResults() {
        return results;
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

    public Graph getTimingTrendGraph() {
        int id;
        try {
            id = Integer.parseInt(Stapler.getCurrentRequest().getParameter(("id")));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Could not parse graph request id as int", e);
            return null;
        }
        return trendsById.get(id).createGraph();
    }

    private void initResults() {
        results = new BuildResults(jsonToResults());
        trendsById = Maps.newHashMap();
        int id = 0;
        for (ScenarioKey key : results.getScenarios().keySet()) {
            trendsById.put(id, new ScenarioTrend(key, build, id++));
        }
    }

    private Object readResolve() {
        initResults();
        return this;
    }
}