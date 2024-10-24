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
import java.util.logging.Logger;

import static com.optimization_component.enums.Operator.*;

@Service
@AllArgsConstructor
public class ElasticSearchServiceImpl<T> implements ElasticSearchService<T> {

    private static final Logger logger = Logger.getLogger(ElasticSearchServiceImpl.class.getName());

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
        logger.info("This is the query : " + query);
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
            an ambiguous case
        */
        BoolQuery.Builder builder = new BoolQuery.Builder();

        //So now let's first define a base-case:
        if (((filter.getField() != null && filter.getValue() != null) && filter.getFilters() == null)) {
            return buildQuery(filter);
        }

        /*
        Now if the filters' list has been received by that list contains only one filter inside it, then in
        that case, the query for that one filter item will be caught in the "must" clause.
        As top level logical operator is only relevant when there is more than one item in the filter's list.
        Having only one filter item in the list makes top-level operator irrelevant
        * */

        else if ((filter.getField() == null && filter.getValue() == null) && filter.getFilters().size() == 1) {
            Filter firstFilter = filter.getFilters().get(0);
            Query query = queryBuilder(firstFilter);
            return builder.must(query).build()._toQuery();
        }


        //if code reaches this point, it means "field" and "value" both are null and "filters' list" is not null &&
        //its size > 1,
        //So this is the perfect case for recursion

        for(Filter currentFilter : filter.getFilters()){
            //going deep into recursion:
            Query query = queryBuilder(currentFilter);
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
        if (value instanceof String ||
                ((value instanceof List<?>)
                && (!((List<?>) value).isEmpty())
                && (((List<?>) value).get(0) instanceof String))) {
            field = filter.getField() + ".keyword";
        } else {
            field = filter.getField();
        }
        switch (filter.getOperator()) {
            case LIKE:
                return Query.of(q -> q.wildcard(w -> w.field(field).value(value + "*")));
            case IS_NULL:
                return Query.of(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field(field)))));
            case NOT_NULL:
                return Query.of(q -> q.exists(e -> e.field(field)));
            case NOT_IN:
                assert value instanceof List<?>;
                List<?> valuesNot_IN = (List<?>) value; // assuming value is a List of values
                return Query.of(q -> q.bool(b -> b
                        .mustNot(mn -> mn
                                .terms(t -> t
                                        .field(field).terms(terms -> terms
                                                .value(valuesNot_IN
                                                        .stream()
                                                        .map(FieldValue::of)
                                                        .toList()))))));
            case IN:
                assert value instanceof List<?>;
                List<?> valuesIN = (List<?>) value; // assuming value is a List of values
                return Query.of(q -> q.terms(t -> t.field(field).terms(terms -> terms.value(valuesIN.stream().map(FieldValue::of).toList()))));
            case NEQ:
                return Query.of(q -> q.bool(b -> b.mustNot(mn -> mn.term(t -> t.field(field).value(FieldValue.of(value))))));
            case EQ:
                return Query.of(q -> q.term(t -> t.field(field).value((FieldValue.of(value)))));
            case GT:
                return Query.of(q -> q.range(r -> r.field(field).gt(JsonData.of(value))));
            case LT:
                return Query.of(q -> q.range(r -> r.field(field).lt((JsonData.of(value)))));
            case LTE:
                return Query.of(q -> q.range(r -> r.field(field).lte(JsonData.of(value))));
            case GTE:
                return Query.of(q -> q.range(r -> r.field(field).gte(JsonData.of(value))));
            case BETWEEN: {
                Map<String, Object> map = (LinkedHashMap<String, Object>) value;
                return Query.of(q -> q.range(r -> r.field(field).gte(JsonData.of(map.get("from"))).lte(JsonData.of(map.get("to")))));
            }
            default:
                throw new UnsupportedOperationException("Unknown comparison operator: " + filter.getOperator());
        }
    }
}