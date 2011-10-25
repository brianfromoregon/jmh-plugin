package com.brianfromoregon;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This is my "Post Build Action" which finds caliper result files, stores them, and adds an action to the build.
 * <p/>
 * Member variables are persisted to job configuration XML with XStream.
 */
public class CaliperResultsRecorder extends Recorder {

    //{@link FileSet} "includes" string, like "foo/bar/*.json"
    private final String results;

    @DataBoundConstructor
    public CaliperResultsRecorder(String results) {
        this.results = results;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("Recording Caliper results: " + results);

        List<String> jsonResults = Lists.newArrayList();

        for (FilePath f : build.getWorkspace().list(results)) {
            jsonResults.add(CharStreams.toString(new InputStreamReader(f.read(), Charsets.UTF_8)));
        }

        if (jsonResults.size() == 0) {
            listener.error("No matching file found. Configuration error?");
            build.setResult(Result.FAILURE);
            return true;
        }

        build.addAction(new CaliperBuildAction(jsonResults.toArray(new String[jsonResults.size()]), build));

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
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
}
