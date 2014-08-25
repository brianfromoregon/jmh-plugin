package com.brianfromoregon.jmh;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import static com.google.common.collect.Lists.transform;

/**
 *
 */
public class FileIndex {
    // In order, older build results first
    final ImmutableList<PublishedFile> results;

    public FileIndex(ImmutableList<PublishedFile> results) {
        this.results = results;
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/csv");
        CSVWriter writer = new CSVWriter(rsp.getWriter());
        writer.writeNext(new String[] {"url", "path"});
        writer.writeAll(transform(results, new Function<PublishedFile, String[]>() {
            @Override
            public String[] apply(PublishedFile result) {
                String[] data = new String[2];
                data[0] = result.getAbsoluteUrl();
                data[1] = result.result.path;
                return data;
            }
        }));
    }

}
