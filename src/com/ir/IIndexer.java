package com.ir;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

public interface IIndexer {
    void addDocuments(List<MyDocument> documents) throws IOException;
    List<Integer> search(MyQuery query) throws IOException, ParseException;
}
