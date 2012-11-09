package com.brianfromoregon;

import com.google.caliper.Json;
import com.google.caliper.Result;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.gson.JsonParseException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is my "Post Build Action" which finds caliper result files, stores them, and adds an action to the build.
 * <p/>
 * Member variables are persisted to job configuration XML with XStream.
 */
public class CaliperResultsRecorder extends Recorder {
    private static final Logger LOGGER = Logger.getLogger(CaliperResultsRecorder.class.getName());

    //{@link FileSet} "includes" string, like "foo/bar/*.json"
    public final String results;
    // Ex. http://microbenchmarks.appspot.com:80/run/
    public final String postUrl;
    // Ex. c9a93074-5752-405d-b126-c85a7221286b
    public final String apiKey;
    // Ex. myproxy:8080
    public final String proxyHostPort;

    @DataBoundConstructor
    public CaliperResultsRecorder(String results, String postUrl, String apiKey, String proxyHostPort) {
        this.results = results.trim();
        this.postUrl = postUrl.trim();
        this.apiKey = apiKey.trim();
        this.proxyHostPort = proxyHostPort.trim();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("Recording Caliper results: " + results);

        List<ParsedFile> jsonResults = readUtf8Results(build);

        if (jsonResults.size() == 0) {
            listener.error("No matching file found. Configuration error?");
            return false;
        }

        List<Result> results = new ArrayList<Result>();
        Iterator<ParsedFile> it = jsonResults.iterator();
        while (it.hasNext()) {
            ParsedFile f = it.next();
            try {
                Result result = Json.getGsonInstance().fromJson(f.content, Result.class);
                if (result == null || result.getRun() == null) {
                    listener.getLogger().println("JSON does not convert to a Result, skipping: " + f.name);
                    it.remove();
                    continue;
                }

                results.add(result);

            } catch (JsonParseException e) {
                listener.getLogger().println("Could not parse file as JSON (see logs for details), skipping: " + f.name);
                LOGGER.log(Level.WARNING, "Could not parse file as JSON, skipping: " + f.name, e);
                it.remove();
            }
        }

        if (results.size() == 0) {
            listener.error("No useful result files kept. Configuration error?");
            return false;
        }

        addBuildAction(build, listener, Lists.transform(jsonResults, new Function<ParsedFile, String>() {
            @Override
            public String apply(ParsedFile input) {
                return input.content;
            }
        }));

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Preventing javac warning
     */
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * This is how Jenkins discovers my publisher.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        public String getDisplayName() {
            return "Publish Caliper microbenchmark results";
        }

    }

    /**
     * Methods overridden in unit test
     */

    List<ParsedFile> readUtf8Results(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        List<ParsedFile> readFiles = Lists.newArrayList();
        for (FilePath f : build.getWorkspace().list(results)) {
            readFiles.add(new ParsedFile(f.getName(), CharStreams.toString(new InputStreamReader(f.read(), Charsets.UTF_8))));
        }
        return readFiles;
    }

    void addBuildAction(AbstractBuild<?, ?> build, BuildListener listener, List<String> jsonResults) {
        CaliperBuildAction action = new CaliperBuildAction(jsonResults.toArray(new String[jsonResults.size()]), build);
        build.addAction(action);

        // Upload results here
        if (!postUrl.isEmpty() && !apiKey.isEmpty()) {
            ResultUploader uploader = new ResultUploader();
            for (Result result : action.jsonToResults()) {
                listener.getLogger().printf("Uploading result for benchmark '%s' to '%s'\n", result.getRun().getBenchmarkName(), postUrl);
                listener.getLogger().println("Outcome: " + uploader.postResults(result, postUrl, apiKey, proxyHostPort));
            }
        }

        // Fail build here
        if (action.getResultDifference().getNumMemoryRegressions() > 0) {
            build.setResult(hudson.model.Result.UNSTABLE);
        }
    }
}

class ParsedFile {
    public final String name;
    public final String content;

    ParsedFile(String name, String content) {
        this.name = name;
        this.content = content;
    }
}
