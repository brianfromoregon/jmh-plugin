Jenkins plugin for [jmh](http://openjdk.java.net/projects/code-tools/jmh). Currently it collects result files as a post-build action then publishes an index file of all results
across all previous builds. This index could be consumed by d3.js to produce visualizations.

JMH can write results in a variety of formats and this plugin currently doesn't care what you use, but json is a good choice
and be selected with jmh's OptionsBuilder. Be sure to write a file name which your Jenkins result pattern will match. Example code:
  
```
new OptionsBuilder()
  ...
  .resultFormat(ResultFormatType.JSON)
  .result(MyBench.class.getName() + ".jmh.json")
  .build();
```

![screenshot](http://i.imgur.com/WzwV0qj.png)
