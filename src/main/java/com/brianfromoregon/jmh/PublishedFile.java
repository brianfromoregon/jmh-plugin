package com.brianfromoregon.jmh;

import jenkins.model.Jenkins;

public class PublishedFile {
    final CollectedFile result;
    // ex: job/test/3/jmh/file/my_file.json
    final String url;

    public PublishedFile(CollectedFile result, String url) {
        this.result = result;
        this.url = url;
    }

    public String getAbsoluteUrl() {
        return Jenkins.getInstance().getRootUrl() + url;
    }
}
