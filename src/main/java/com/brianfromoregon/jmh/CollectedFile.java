package com.brianfromoregon.jmh;

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
}
