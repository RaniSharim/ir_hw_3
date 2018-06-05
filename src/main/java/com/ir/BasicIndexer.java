package main.java.com.ir;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicIndexer implements IIndexer {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Directory index = new RAMDirectory();

    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    int NumberOfDocs = 0;
    float Threshhold;

    public BasicIndexer(float threshold) {
        Threshhold = threshold;
    }
    @Override
    public void addDocuments(List<MyDocument> documents) throws IOException {
        // Add all documents to lucene index, one by one
        IndexWriter w = new IndexWriter(index, config);
        for (MyDocument doc : documents) {
            addDocument(doc, w);
        }
        w.close();
        NumberOfDocs = documents.size();
    }

    private void addDocument(MyDocument doc, IndexWriter w) throws IOException {
        Document luceneDoc = new Document();
        luceneDoc.add(new StoredField("docId", doc.DocId));
        luceneDoc.add(new TextField ("body", doc.Text, Field.Store.YES));
        w.addDocument(luceneDoc);
    }


    @Override
    public List<Integer> search(MyQuery query) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        // This sets the similarity to classic tf/idf
        searcher.setSimilarity(new ClassicSimilarity());

        // Do the search, lucene handles breaking the query/panctuation/stop words
        Query q = new QueryParser("body", analyzer).parse(query.Text);

        TopDocs topDocs = searcher.search(q, NumberOfDocs);
        ScoreDoc[] hits = topDocs.scoreDocs;

        List<Integer> results = new ArrayList<>();

        for (ScoreDoc hit: hits) {
            if (hit.score >= Threshhold) {
                Document d = searcher.doc(hit.doc);
                results.add(Integer.parseInt(d.get("docId")));
            }
        }

        Collections.sort(results);

        return results;
    }
}
