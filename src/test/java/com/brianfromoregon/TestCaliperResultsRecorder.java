package com.brianfromoregon;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class TestCaliperResultsRecorder {

    String simpleMemBenchJson;
    String nonCaliperJson;
    AbstractBuild<?, ?> build;
    Launcher launcher;
    BuildListener listener;
    CaliperResultsRecorder instance;
    List<String> inputResults;
    List<String> outputResults;

    @Before
    public void loadSimpleMemBench() throws Exception {
        simpleMemBenchJson = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/simpleMemBench.json"), Charsets.UTF_8));
        nonCaliperJson = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/nonCaliper.json"), Charsets.UTF_8));
        build = mock(AbstractBuild.class);
        listener = mock(BuildListener.class, RETURNS_DEEP_STUBS);
        launcher = null;
        inputResults = Lists.newArrayList();
        outputResults = Lists.newArrayList();
        instance = new CaliperResultsRecorder("", "", "", "") {
            @Override
            List<ParsedFile> readUtf8Results(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
                return Lists.transform(inputResults, new Function<String, ParsedFile>() {
                    @Override
                    public ParsedFile apply(String input) {
                        return new ParsedFile("", input);
                    }
                });
            }

            @Override
            void addBuildAction(AbstractBuild<?, ?> build, BuildListener listener, List<String> jsonResults) {
                outputResults.addAll(jsonResults);
            }
        };
    }

    @Test
    public void no_results_is_failure() throws Exception {
        assertFalse(instance.perform(build, launcher, listener));
        assertEquals(0, outputResults.size());
    }

    @Test
    public void all_bad_results_is_failure() throws Exception {
        inputResults.add(simpleMemBenchJson.replace(':', ';')); // bad syntax
        inputResults.add(nonCaliperJson); // Not a Caliper result
        assertFalse(instance.perform(build, launcher, listener));
        assertEquals(0, outputResults.size());
    }

    @Test
    public void bad_results_are_skipped() throws Exception {
        inputResults.add(simpleMemBenchJson);
        inputResults.add(simpleMemBenchJson.replace(':', ';'));
        inputResults.add(simpleMemBenchJson);
        assertTrue(instance.perform(build, launcher, listener));
        assertEquals(2, outputResults.size());
    }
}

