package com.brianfromoregon;

import com.google.caliper.Json;
import com.google.caliper.Result;
import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: sad that I'm storing AbstractBuild for jelly, gotta be a better way
 * TODO: is this slave friendly?
 */
public class CaliperBuildAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(CaliperBuildAction.class.getName());
    private final String[] jsonResults;
    public final AbstractBuild<?, ?> build;

    private transient List<Result> results;

    public CaliperBuildAction(String[] jsonResults, AbstractBuild<?, ?> build) {
        this.jsonResults = jsonResults;
        this.build = build;

        initResults();
    }

    private void initResults() {
        results = Lists.newArrayList();
        for (String r : jsonResults) {
            try {
                results.add(Json.getGsonInstance().fromJson(r, Result.class));
            } catch (JsonSyntaxException e) {
                LOGGER.log(Level.SEVERE, "Could not parse Caliper result file as JSON, skipping", e);
            }
        }
    }

    private Object readResolve() {
        initResults();
        return this;
    }

    public List<Result> getResults() {
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
}