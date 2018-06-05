package main.java.com.ir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class MyDocument {
    int DocId;
    String Text;

    static List<MyDocument> readDocuments(String filename) throws IOException {
        List<String> documentLines = Files.readAllLines(Paths.get(filename));
        List<MyDocument> documents = new ArrayList<>();

        MyDocument currentDocument = null;
        StringBuffer currentDocumentText = null;

        // Go over all the lines in the document file
        for (String documentLine : documentLines) {
            // if it is a header line
            if (documentLine.startsWith("*TEXT")) {

                // Add the current document to the document list
                if (currentDocument != null) {
                    currentDocument.Text = currentDocumentText.toString().toUpperCase();
                    documents.add(currentDocument);
                }

                currentDocument = new MyDocument();
                currentDocumentText = new StringBuffer();

                // Parse the new header
                String[] titleStrings = documentLine.split("\\W+");

                currentDocument.DocId = Integer.parseInt(titleStrings[2]);
            }
            // Otherwise, add the text line to the current document body
            else {
                currentDocumentText.append(documentLine);
            }
        }

        return documents;
    }
}
