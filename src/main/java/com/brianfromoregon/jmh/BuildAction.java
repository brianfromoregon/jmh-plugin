package com.brianfromoregon.jmh;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.util.logging.Logger;

import static com.google.common.collect.Lists.transform;
import static java.lang.String.format;

public class BuildAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(BuildAction.class.getName());
    private final ImmutableList<CollectedFile> results;
    public final AbstractBuild<?, ?> build;

    private transient ImmutableMap<String, CollectedFile> resultsByName;

    public BuildAction(ImmutableList<CollectedFile> results, AbstractBuild<?, ?> build) {
        this.results = results;
        this.build = build;

        initResults();
    }

    /**
     * /jmh/file/name
     */
    public String getFile(String name) {
        CollectedFile file = resultsByName.get(name);
        if (file == null) {
            return null;
        }

        return file.content;
    }

    /**
     * /jmh/allResults
     */
    public FileIndex getAllResults() {
        ImmutableList.Builder<PublishedFile> list = ImmutableList.builder();
        for (AbstractBuild<?, ?> build = this.build; build != null; build = build.getPreviousBuiltBuild()) {

            BuildAction buildAction = build.getAction(BuildAction.class);
            if (buildAction == null) {
                continue;
            }

            final String buildUrl = build.getUrl();
            list.addAll(transform(buildAction.results, new Function<CollectedFile, PublishedFile>() {
                @Override
                public PublishedFile apply(CollectedFile collectedFile) {
                    String url = format("%sjmh/file/%s", buildUrl, collectedFile.name());
                    return new PublishedFile(collectedFile, url);
                }
            }));
        }
        return new FileIndex(list.build().reverse());
    }

    /**
     * For BuildAction/summary.jelly
     */
    public String getSummary() {
        return format("Collected %d result file%s",
                results,
                results.size() == 1 ? "" : "s");
    }

    @Override
    public String getIconFileName() {
        return "/plugin/caliper-ci/caliper.png";
    }

    @Override
    public String getDisplayName() {
        return "JMH";
    }

    @Override
    public String getUrlName() {
        return "jmh";
    }

    private void initResults() {
        Builder<String, CollectedFile> map = ImmutableMap.builder();
        for (CollectedFile result : results) {
            map.put(result.name(), result);
        }
        this.resultsByName = map.build();
    }

    private Object readResolve() {
        initResults();
        return this;
    }
}