package com.optimization_component.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.optimization_component.enums.LogicalOperator;
import com.optimization_component.payload.LeafFilter;
import com.optimization_component.service.interfaces.ElasticSearchService;
import com.optimization_component.payload.Payload;
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


    /*
    Search is incomplete as of now. A discussion is required on how the searches have
    to be performed, on what basis, what parameters etc.
    */
    @Override
    public List<T> search(String indexName, Payload payload) throws IOException {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        List<LeafFilter> leafFilters = payload.getFilters();

        for(int i = 0; i< leafFilters.size(); i++){
            LeafFilter leafFilter = leafFilters.get(i);

            //We will check the second leafFilter's operator for deciding whether first will
            //go into "should" or "must" [BUT THIS WILL BE DONE ONLY FOR FIRST FILTER]
            if(i == 0) {
                if(leafFilters.size() == 1){
                    //it means only one leafFilter is there and this will be added to "must" clause
                    addToMustClause(boolQuery, leafFilter);
                }
                else {
                    //extract logicalOperator from second leafFilter
                    LogicalOperator logicalOperatorOfSecondFilter = leafFilters.get(1).getLogicalOperator();
                    if(logicalOperatorOfSecondFilter.equals(LogicalOperator.OR)){
                        //so in this case this first leafFilter will be added to the "should" clause
                        addToShouldClause(boolQuery, leafFilter);
                    } else {
                        //this first leafFilter will be added to the "must" clause
                        addToMustClause(boolQuery, leafFilter);
                    }
                }
            } else {
                if(leafFilter.getLogicalOperator().equals(LogicalOperator.OR)){
                    addToShouldClause(boolQuery, leafFilter);
                } else {
                    addToMustClause(boolQuery, leafFilter);
                }
            }
        }

        Query query = Query.of(q -> q.bool(boolQuery.build()));

        //See now we have the query now I want to send this query to search api of elasticsearch and receive the result
        //so how to do that here?
        var searchResponse = elasticClient.search(s -> s
                .index(indexName)
                .query(query),
                Object.class
        );

        return searchResponse.hits().hits().stream()
                .map(hit -> (T) hit.source())
                .toList();

    }

    private void addToShouldClause(BoolQuery.Builder boolQuery, LeafFilter leafFilter) {
        boolQuery.should(buildQuery(leafFilter));
    }

    private void addToMustClause(BoolQuery.Builder boolQuery, LeafFilter leafFilter) {
        boolQuery.must(buildQuery(leafFilter));
    }

    private Query buildQuery(LeafFilter leafFilter) {


        Object value = leafFilter.getValue();
        String field;

        if(value instanceof String){
            field = leafFilter.getField() + ".keyword";
        } else {
            field = leafFilter.getField();
        }

        switch (leafFilter.getComparisonOperator()) {
            case EQUALS:
                return Query.of(q -> q.term(t -> t.field(field).value((FieldValue.of(value)))));

            case GREATER_THAN:
                return Query.of(q -> q.range(r -> r.field(field).gt(JsonData.of(value))));

            case LESS_THAN:
                return Query.of(q -> q.range(r -> r.field(field).lt((JsonData.of(value)))));

            case BETWEEN: {
                Map<String,Object> map = (LinkedHashMap<String, Object>) value;
                return Query.of(q -> q.range(r -> r.field(field).gte(JsonData.of(map.get("from"))).lte(JsonData.of(map.get("to")))));
            }
            default:
                    throw new UnsupportedOperationException("Unknown comparison operator: " + leafFilter.getComparisonOperator());
        }
    }
}