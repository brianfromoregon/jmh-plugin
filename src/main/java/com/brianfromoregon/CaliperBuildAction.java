package com.brianfromoregon;

import com.google.caliper.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.util.EnumSet;
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

    /**
     * Each of the supplied JSON results must carry at least one MeasurementType of MEMORY or INSTANCE.
     */
    public CaliperBuildAction(String[] jsonResults, AbstractBuild<?, ?> build) {
        this.jsonResults = jsonResults;
        this.build = build;

        initResults();
    }

    /**
     * For index.jelly
     */
    public Iterable<ScenarioMemoryResultChange> getScenarioMemoryResultChanges() {
        return getMemoryResultDifference().getChanges(EnumSet.complementOf(EnumSet.of(ScenarioMemoryResultChange.Type.NO_RESULT)));
    }

    /**
     * For summary.jelly
     */
    public String getSummary() {
        return getMemoryResultDifference().getSummary();
    }

    public BuildMemoryResultDifference getMemoryResultDifference() {
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
        
        return new BuildMemoryResultDifference(prevResults, results);
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