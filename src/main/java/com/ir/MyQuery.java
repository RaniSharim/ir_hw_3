package com.ir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MyQuery {
    public int QueryId;
    public String Text;

    public static List<MyQuery> readQueries(String filename) throws IOException {
        List<String> queryLines = Files.readAllLines(Paths.get(filename));
        List<MyQuery> queries = new ArrayList<MyQuery>();

        MyQuery currentQuery = null;
        StringBuffer currentQueryText = null;

        // Go over all the lines in the query file
        // if it is a header line
        for (String queryLine : queryLines)
            if (queryLine.startsWith("*FIND")) {
                // Add the current query to the query list
                if (currentQuery != null) {
                    currentQuery.Text = currentQueryText.toString().toUpperCase();
                    queries.add(currentQuery);
                }

                currentQuery = new MyQuery();
                currentQueryText = new StringBuffer();

                // Parse the new header
                String[] titleStrings = queryLine.split("\\W+");

                currentQuery.QueryId = Integer.parseInt(titleStrings[2]);
            }
            // Otherwise, add the text line to the current query body
            else {
                currentQueryText.append(queryLine);
            }

        return queries;
    }
}
