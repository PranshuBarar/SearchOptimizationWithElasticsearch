package com.optimization_component.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.optimization_component.payload.Filter;
import com.optimization_component.service.interfaces.ElasticSearchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.optimization_component.enums.Operator.*;

@Service
@AllArgsConstructor
public class ElasticSearchServiceImpl<T> implements ElasticSearchService<T> {
    private final ElasticsearchClient elasticClient;

    @Override
    public void save(String indexName, String _id, T entity) throws IOException {
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
    public List<T> search(String indexName, Filter filter) throws Exception {
        Query query = queryBuilder(filter);

        var searchResponse = elasticClient.search(s -> s
                .index(indexName)
                .query(query),
                Object.class
        );

        return searchResponse.hits().hits().stream()
                .map(hit -> (T) hit.source())
                .toList();

    }

    private Query queryBuilder(Filter filter) {
        /*
            Below is the structure of Filter class
            public class Filter {
                private Operator operator;
                        +
                //----------------
                private String field;
                private Object value;
                //----------------

                        OR

                //----------------
                private List<Filter> filters;
                //----------------
            }
            There can be two cases:
            EITHER
            1) The "field" && "value" both will be null and "filters"
            list will be non-null -> which means we have to go more deep into recursion
            OR
            2) The "filters" field will be null and "field" && "value"
            both will be non-null -> which means it's a leaf filter, and it's a base-case
            and we have to return

            !! Note: It's very important to keep in mind that both:
            => "field" && "value" pair
            and
            => "filters"
            can't neither be "null" nor "non-null" at the same time, as it will be
            ambiguous case
        */
        BoolQuery.Builder builder = new BoolQuery.Builder();

        //So now let's first define base-case:
        if (((filter.getField() != null && filter.getValue() != null) && filter.getFilters() == null)) {
            return buildQuery(filter);
        } else if ((filter.getField() == null && filter.getValue() == null) && filter.getFilters().size() == 1) {
            return builder.must(buildQuery(filter.getFilters().get(0))).build()._toQuery();
        }


        //if code reaches this point it means "field" and "value" both are null and "filters' list" is not null &&
        //its size > 1
        //So this is the perfect case for recursion

        for(Filter currentFilter : filter.getFilters()){
            //going deep into recursion:
            Query query = queryBuilder(currentFilter);

            if(filter.getFilters().size() == 1){
                return query;
            }
            if(filter.getOperator() == AND) {
                builder.must(query);
            }
            else if (filter.getOperator() == OR) {
                builder.should(query);
            }
        }
        return builder.build()._toQuery();
    }

    private Query buildQuery(Filter filter) {
        Object value = filter.getValue();
        String field;

        if (value instanceof String) {
            field = filter.getField() + ".keyword";
        } else {
            field = filter.getField();
        }
        switch (filter.getOperator()) {
            case EQ:
                return Query.of(q -> q.term(t -> t.field(field).value((FieldValue.of(value)))));

            case GREATER_THAN:
                return Query.of(q -> q.range(r -> r.field(field).gt(JsonData.of(value))));

            case LESS_THAN:
                return Query.of(q -> q.range(r -> r.field(field).lt((JsonData.of(value)))));

            case BETWEEN: {
                Map<String, Object> map = (LinkedHashMap<String, Object>) value;
                return Query.of(q -> q.range(r -> r.field(field).gte(JsonData.of(map.get("from"))).lte(JsonData.of(map.get("to")))));
            }
            default:
                throw new UnsupportedOperationException("Unknown comparison operator: " + filter.getOperator());
        }
    }
}