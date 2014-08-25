package com.brianfromoregon.jmh;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.io.CharStreams;
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

import static org.apache.commons.lang.StringUtils.strip;

/**
 * This is my "Post Build Action" which finds result files, stores them, and adds an action to the build.
 * <p/>
 * Member variables are persisted to job configuration XML with XStream.
 */
public class ResultsRecorder extends Recorder {

    //{@link FileSet} "includes" string, like "foo/bar/*.json"
    public final String results;

    @DataBoundConstructor
    public ResultsRecorder(String results) {
        this.results = results.trim();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        listener.getLogger().println("Recording results: " + results);

        ImmutableList<CollectedFile> results = readUtf8Results(build);

        BuildAction action = new BuildAction(results, build);
        build.addAction(action);

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
            return "Publish JMH benchmark results";
        }
    }

    private ImmutableList<CollectedFile> readUtf8Results(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        String workspace = build.getWorkspace().getRemote();
        Builder<CollectedFile> list = ImmutableList.builder();
        for (FilePath f : build.getWorkspace().list(results)) {
            String content = CharStreams.toString(new InputStreamReader(f.read(), Charsets.UTF_8));
            String name = strip(f.getRemote().substring(workspace.length()).replace('\\', '/'), "/");
            list.add(new CollectedFile(name, content));
        }
        return list.build();
    }
}
