package com.brianfromoregon;

import com.google.caliper.*;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

public class CaliperResultsRecorder extends Recorder {

    /**
     * {@link FileSet} "includes" string, like "foo/bar/*.json"
     */
    private final String results;

    @DataBoundConstructor
    public CaliperResultsRecorder(String results) {
        this.results = results == null ? "*.caliper.json" : results;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("Recording Caliper results: " + results);

        File outDir = storageDirFor(build);
        outDir.mkdir();

        int numFiles = build.getWorkspace().copyRecursiveTo(results, new FilePath(outDir));
        if (numFiles == 0) {
            listener.error("No matching file found. Configuration error?");
            build.setResult(Result.FAILURE);
            return true;
        }

        // work with files copied to the local dir
        FileSet fs = new FileSet();
        fs.setDir(outDir);
        DirectoryScanner ds = fs.getDirectoryScanner(new Project());
        String[] includedFiles = ds.getIncludedFiles();

        File prevDir = storageDirFor(build.getPreviousNotFailedBuild());

        for (String f : includedFiles) {
            File file = new File(ds.getBasedir(), f);

            listener.getLogger().println(file);

            /* Hard coding UTF-8: http://code.google.com/p/caliper/issues/detail?id=134 */
            com.google.caliper.Result result =
                    Json.getGsonInstance().fromJson(Files.toString(file, Charsets.UTF_8), com.google.caliper.Result.class);

            for (Map.Entry<Scenario, ScenarioResult> includedFile : result.getRun().getMeasurements().entrySet()) {
                Scenario scenario = includedFile.getKey();
                ScenarioResult sResult = includedFile.getValue();
                MeasurementSet memory = sResult.getMeasurementSet(MeasurementType.MEMORY);
                MeasurementSet instance = sResult.getMeasurementSet(MeasurementType.INSTANCE);

                listener.getLogger().println(memory);
                listener.getLogger().println(instance);

            }
        }
        return true;
    }

    private static File storageDirFor(AbstractBuild<?, ?> build) {
        if (build == null)
            return null;
        return new File(build.getRootDir(), "caliper");
    }

    public String getResults() {
        return results;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

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
