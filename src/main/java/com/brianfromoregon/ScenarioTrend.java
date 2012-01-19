package com.brianfromoregon;

import com.google.caliper.MeasurementType;
import com.google.caliper.ScenarioResult;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.AbstractBuild;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class ScenarioTrend {
    public final int id;
    public final AbstractBuild<?, ?> build;
    public final ScenarioKey scenarioKey;

    protected ScenarioTrend(ScenarioKey scenarioKey, AbstractBuild<?, ?> build, int id) {
        this.scenarioKey = scenarioKey;
        this.build = build;
        this.id = id;
    }

    public String getBenchmarkName() {
        return scenarioKey.getBenchmarkName();
    }

    public Map<String, String> getParameters() {
        return scenarioKey.getScenario().getVariables();
    }

    private Iterable<BuildTimingResult> allResults() {
        List<BuildTimingResult> results = Lists.newArrayList();
        for (AbstractBuild<?, ?> build = this.build; build != null; build = build.getPreviousBuiltBuild()) {

            CaliperBuildAction buildAction = build.getAction(CaliperBuildAction.class);
            if (buildAction == null) {
                continue;
            }

            ScenarioResult result = buildAction.getResults().getScenarios().get(scenarioKey);
            if (result == null)
                continue;

            results.add(new BuildTimingResult(result.getMeasurementSet(MeasurementType.TIME).meanRaw(), build));
        }
        return results;
    }

    private SubSecond finestUnit() {
        return SubSecond.finestFor(Iterables.transform(allResults(), new Function<BuildTimingResult, Double>() {
            @Override
            public Double apply(BuildTimingResult buildTimingResult) {
                return buildTimingResult.nanos;
            }
        }));
    }

    public Graph createGraph() {
        return new Graph(-1, 500, 150) {
            @Override
            protected JFreeChart createGraph() {
                DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSet = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

                SubSecond finest = finestUnit();
                for (BuildTimingResult result : allResults()) {
                    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(result.build);
                    dataSet.add(finest.fromNanos(result.nanos), finest.name, label);
                }

                JFreeChart chart = ChartFactory.createLineChart(
                        null, // chart title
                        null, // x axis
                        finest.name, // y axis label
                        dataSet.build(), // data
                        PlotOrientation.VERTICAL, // orientation
                        false, // include legend
                        true, // tooltips
                        false // urls
                );
                chart.setBackgroundPaint(Color.white);
                LineAndShapeRenderer r = (LineAndShapeRenderer) chart.getCategoryPlot().getRenderer();
                r.setSeriesShape(0, new Rectangle(-2, -2, 4, 4));
                r.setSeriesShapesVisible(0, true);
                r.setSeriesShapesFilled(0, true);
                return chart;
            }
        };
    }

    public Stats getStatistics() {
        SubSecond finest = finestUnit();
        Stats stats = new Stats(finest);

        for (BuildTimingResult result : allResults()) {
            stats.addValue(result.nanos);
        }

        return stats;
    }

    private static class BuildTimingResult {
        public final AbstractBuild<?, ?> build;
        public final double nanos;

        BuildTimingResult(double nanos, AbstractBuild<?, ?> build) {
            this.nanos = nanos;
            this.build = build;
        }
    }
}
