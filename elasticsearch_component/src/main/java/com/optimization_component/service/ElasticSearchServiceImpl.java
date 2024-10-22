package com.optimization_component.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.optimization_component.enums.LogicalOperator;
import com.optimization_component.payload.Filter;
import com.optimization_component.payload.Payload;
import com.optimization_component.service.interfaces.ElasticSearchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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



    @Override
    public List<T> search(String indexName, Payload payload) throws IOException {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        List<Filter> filters = payload.getFilters();
        for(int i=0; i<filters.size(); i++){
            Filter filter = filters.get(i);
            //We will check the second filter's operator for deciding whether first will
            //go into "should" or "must" [BUT THIS WILL BE DONE ONLY FOR FIRST FILTER]
            if(i == 0) {
                if(filters.size() == 1){
                    //it means only one filter is there and this will be added to "must" clause
                    addToMustClause(boolQuery,filter);
                }
                else {
                    //extract logicalOperator from second filter
                    LogicalOperator logicalOperatorOfSecondFilter = filters.get(1).getLogicalOperator();
                    if(logicalOperatorOfSecondFilter.equals(LogicalOperator.OR)){
                        //so in this case, this first filter will be added to the "should" clause
                        addToShouldClause(boolQuery,filter);
                    } else {
                        //this first filter will be added to the "must" clause
                        addToMustClause(boolQuery,filter);
                    }
                }
            } else {
                if(filter.getLogicalOperator().equals(LogicalOperator.OR)){
                    addToShouldClause(boolQuery,filter);
                } else {
                    addToMustClause(boolQuery,filter);
                }
            }
        }
        Query query = Query.of(q -> q.bool(boolQuery.build()));
        //Now we have the query now we want to send this query to search api of elasticsearch and receive the result
        var searchResponse = elasticClient.search(s -> s
                .index(indexName)
                .query(query),
                Object.class
        );
        return searchResponse.hits().hits().stream()
                .map(hit -> (T) hit.source())
                .toList();
    }

    private void addToShouldClause(BoolQuery.Builder boolQuery, Filter filter) {
        boolQuery.should(buildQuery(filter));
    }

    private void addToMustClause(BoolQuery.Builder boolQuery, Filter filter) {
        boolQuery.must(buildQuery(filter));
    }

    private Query buildQuery(Filter filter) {


        Object value = filter.getValue();
        String field;
        if(value instanceof String){
            field = filter.getField() + ".keyword";
        } else {
            field = filter.getField();
        }

        switch (filter.getComparisonOperator()) {
            case EQUALS:
                return Query.of(q -> q.term(t -> t.field(field).value((FieldValue.of(value)))));
            case GREATER_THAN:
                return Query.of(q -> q.range(r -> r.field(field).gt(JsonData.of(value))));
            case LESS_THAN:
                return Query.of(q -> q.range(r -> r.field(field).lt((JsonData.of(value)))));
            case BETWEEN: {
                Map<String,Object> map = (LinkedHashMap<String, Object>) value;
                return Query.of(q -> q.range(r -> r
                        .field(field)
                        .gte(JsonData.of(map.get("from")))
                        .lte(JsonData.of(map.get("to")))));
            }
            default:
                    throw new UnsupportedOperationException("Unknown comparison operator: "
                            + filter.getComparisonOperator());
        }
    }
}






