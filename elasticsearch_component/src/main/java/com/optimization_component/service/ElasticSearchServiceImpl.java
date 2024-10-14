package com.optimization_component.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.optimization_component.service.interfaces.ElasticSearchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class ElasticSearchServiceImpl<T> implements ElasticSearchService<T> {

    private final ElasticsearchClient elasticClient;

    @Override
    public void save(String indexName, String _id,  T entity) throws IOException {

        elasticClient.index(i -> i
                .index(indexName)
                .id(_id)
                .document(entity)
        );
    }

    @Override
    public void delete(String indexName, String _id) throws IOException {

        elasticClient.delete(d -> d
                .index(indexName)
                .id(_id)
        );
    }


    /*
    Search is incomplete as of now. A discussion is required on how the searches have
    to be performed, on what basis, what parameters etc.
    */
    @Override
    public List<T> search(String keyword) {
        return List.of();
    }

}




//    @Override
//    public void updateField(String indexName, String _id, String fieldName, Object newValue) {
//
//    }
