package main.java.com.ir;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.DocFreqComparator;
import org.apache.lucene.misc.TermStats;
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
import java.util.*;

public class BasicIndexer implements IIndexer {
    CharArraySet stopWords;
    Directory index;

    int NumberOfDocs = 0;
    float Threshhold;

    // Figure out and store the stop words
    private void getStopwords(List<MyDocument> documents) throws Exception {

        // Add all documents with no stop words, to get top 20 words
        StandardAnalyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        for (MyDocument doc : documents) {
            addDocument(doc, w);
        }
        w.close();

        IndexReader reader = DirectoryReader.open(index);

        // Get the 20 most common words
        DocFreqComparator cmp = new DocFreqComparator();
        TermStats[] highFreqTerms = HighFreqTerms.getHighFreqTerms(reader, 20, "body", cmp);

        Set<String> terms = new HashSet<>();

        for (TermStats ts : highFreqTerms) {
            terms.add(ts.termtext.utf8ToString());
        }

        stopWords = CharArraySet.copy(terms);
    }

    public BasicIndexer(float threshold) {
        Threshhold = threshold;
    }
    @Override
    public void addDocuments(List<MyDocument> documents) throws Exception {

        // Get top 20 words as stopwords
        getStopwords(documents);
        // Init a new standard analyzer with top 20 words as stopwords
        StandardAnalyzer analyzer = new StandardAnalyzer(stopWords);

        // Add all documents to lucene index, one by one
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        for (MyDocument doc : documents) {
            addDocument(doc, w);
        }
        w.close();
        this.index = index;

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

        // Init a new standard analyzer with top 20 words as stopwords
        StandardAnalyzer analyzer = new StandardAnalyzer(stopWords);

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

        // Sort the results by docID
        Collections.sort(results);

        return results;
    }
}
