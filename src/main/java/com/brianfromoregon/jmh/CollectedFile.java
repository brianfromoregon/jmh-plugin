package com.brianfromoregon.jmh;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Function;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

import static com.google.common.collect.Lists.transform;

/**
 *
 */
public class CollectedFile {
    // ex: $WORKSPACE/path/to/file.json would have path of path/to/file.json
    final String path;
    // the text file content
    final String content;

    public CollectedFile(String path, String content) {
        this.path = path;
        this.content = content;
    }

    /**
     * path of path/to/file.json would have name of path_to_file.json
     */
    String name() {
        return path.replace('/', '_');
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/plain");
        rsp.getWriter().append(content);
    }
}
