package com.ir;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
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
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImprovedIndexer implements IIndexer {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Directory index = new RAMDirectory();

    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    int NumberOfDocs = 0;
    float Threshhold;

    public ImprovedIndexer(float threshold) {
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

    private void replaceKnownNames(MyDocument doc) {
        doc.Text = doc.Text.replace("VIET NAM", "VIETNAM");
        doc.Text = doc.Text.replace("VIET CONG", "VIETCONG");
    }

    private void addDocument(MyDocument doc, IndexWriter w) throws IOException {
        Document luceneDoc = new Document();

        // Try to convert some terms to get more relevant results. Doesn't really seem to work
        replaceKnownNames(doc);

        luceneDoc.add(new StoredField("docId", doc.DocId));
        luceneDoc.add(new TextField("body", doc.Text, Field.Store.YES));
        w.addDocument(luceneDoc);
    }


    @Override
    public List<Integer> search(MyQuery query) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        // This sets the similarity to classic tf/idf
        searcher.setSimilarity(new ClassicSimilarity() {
            @Override
            public float tf(float freq) {
                return super.tf(freq);
            }

            @Override
            public float lengthNorm(int length) {
                return super.lengthNorm(length);
            }

            @Override
            public float idf(long docFreq, long docCount) {
               return super.idf(docFreq, docCount);
            }

        });

        // Try to convert some terms to get more relevant results. Doesn't really seem to work
        enhanceQueryTerms(query);

        // Do the search, lucene handles breaking the query/panctuation/stop words
        Query q = new QueryParser("body", analyzer).parse(query.Text);

        TopDocs topDocs = searcher.search(q, NumberOfDocs);
        ScoreDoc[] hits = topDocs.scoreDocs;

        // This is a dynamic threshold, to be relative to all the docs
        if (hits.length > 0) {
            Threshhold = hits[hits.length/10].score;
        }

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

    private void enhanceQueryTerms(MyQuery query) {
        if (query.Text.contains("COUP")) {
            query.Text += "REBEL";
        }

//        if (query.Text.contains("ALTERNATIVE")) {
//            query.Text += "PROGRAM PLAN";
//        }

        query.Text = query.Text.replace("VIET NAM", "VIETNAM");
        query.Text = query.Text.replace("VIET CONG", "VIETCONG");
    }
}
