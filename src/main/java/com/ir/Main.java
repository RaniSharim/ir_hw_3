package com.ir;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("path to parameters.txt is missing");
            System.exit(1);
        }

        try {
            Configuration config = Configuration.readConfiguration(args[0]);
            IIndexer indexer;

            if (config.retrievalAlgorithm.toLowerCase().equals("basic")) {
                indexer = new BasicIndexer(1.0f);
            }
            else {
                indexer = new ImprovedIndexer(1.8f);
            }

            indexer.addDocuments(MyDocument.readDocuments(config.docFile));

            List<MyQuery> queries = MyQuery.readQueries(config.queryFile);

            List<List<Integer>> truth = Files.readAllLines(Paths.get("truth.txt")).stream().filter(s -> !s.isEmpty()).map(
                    t -> Arrays.stream(t.split("\\W+")).skip(1).map(s-> Integer.parseInt(s)).collect(Collectors.toList())
            ).collect(Collectors.toList());

            StringBuffer resultStr = new StringBuffer();

            for (MyQuery query: queries) {
                List<Integer> results = indexer.search(query);
                List<Integer> queryTruth = truth.get(query.QueryId - 1);
//                System.out.println(query.QueryId + " " + String.join(" ",  results.stream().map(i -> i.toString()).collect(Collectors.toList())));
                resultStr.append(query.QueryId + " " + String.join(" ",  results.stream().map(i -> i.toString()).collect(Collectors.toList())));


                float selected = results.size();
                float relevant = queryTruth.size();

                results.retainAll(queryTruth);
                float tp = results.size();

                float precision = (selected>0?tp/selected:0);
                float recall = (tp/relevant);
                float f1Score = (precision + recall > 0) ? (precision*recall) / (precision + recall) : 0f;
                System.out.println( precision + "\t" + recall + "\t" + f1Score);
            }

            Files.write(Paths.get(config.outputFile), resultStr.toString().getBytes());
        }
        catch (Exception e) {
            System.out.println(e.toString());
            System.out.println(e.getStackTrace().toString());
        }

    }
}
