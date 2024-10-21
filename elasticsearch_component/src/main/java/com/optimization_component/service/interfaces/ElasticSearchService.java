package com.optimization_component.service.interfaces;

import com.optimization_component.payload.Filter;

import java.io.IOException;
import java.util.List;

public interface ElasticSearchService<T> {

    void save(String indexName, String _id,  T entity) throws IOException;
    void delete(String indexName, String _id) throws IOException;
    List<T> search(String indexName, Filter filter) throws Exception;

}