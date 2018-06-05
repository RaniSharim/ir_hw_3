package main.java.com.ir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Configuration {
    String queryFile;
    String docFile;
    String outputFile;
    String retrievalAlgorithm;

    static Configuration readConfiguration(String filename) throws IOException {
        List<String> configurationLines = Files.readAllLines(Paths.get(filename));

        Configuration config = new Configuration();
        config.queryFile = configurationLines.get(0).split("=")[1];
        config.docFile = configurationLines.get(1).split("=")[1];
        config.outputFile = configurationLines.get(2).split("=")[1];
        config.retrievalAlgorithm = configurationLines.get(3).split("=")[1];

        return config;
    }
}
