package org.infobip.voice.genapi.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.model.HttpHeader;
import org.infobip.voice.genapi.repository.mapper.SingleResponseEndpointRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Repository
@AllArgsConstructor
public class SingleResponseEndpointRepository {

    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = DatabaseException.class)
    public Integer save(SingleResponseEndpoint singleResponseEndpoint) throws DatabaseException {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withSchemaName("voip").withTableName("SingleResponseEndpoint").usingGeneratedKeyColumns("Id");

        Map<String, Object> parameters = Map.of(
                "HttpMethod", singleResponseEndpoint.getHttpMethod().toString(),
                "Response", singleResponseEndpoint.getResponse());
        try {
            Number singleResponseEndpointId = jdbcInsert.executeAndReturnKey(parameters);
            singleResponseEndpoint.getHttpHeaders().forEach(e -> insertHeaderToDb(singleResponseEndpointId, e));
            singleResponseEndpoint.setId(singleResponseEndpointId.intValue());
            return singleResponseEndpointId.intValue();
        } catch (Exception e) {
            log.error("Error while trying to save Single Response Endpoint to database, rolling back");
            throw new DatabaseException(e.getMessage());
        }
    }

    public SingleResponseEndpoint getById(Integer id) {
        try {
            return jdbcTemplate.queryForObject(String.format("select * from voip.SingleResponseEndpoint sre left join voip.EndpointHeader eh on eh.EndpointId=sre.Id where sre.id=%d", id), new SingleResponseEndpointRowMapper());
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            log.warn(String.format("Could not read Single Endpoint with id %s from database, message: %s", id, e.getMessage()));
        }
        return null;
    }

    public List<SingleResponseEndpoint> getAll() {
        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from voip.SingleResponseEndpoint sre left join voip.EndpointHeader eh on eh.EndpointId=sre.Id");
            Map<Integer, SingleResponseEndpoint> singleResponseEndpointMap = new HashMap<>();

            while (sqlRowSet.next()) {
                int singlResponseEndpointId = sqlRowSet.getInt("Id");
                String httpHeaderName = sqlRowSet.getString("Name");
                String httpHeaderValue = sqlRowSet.getString("Value");

                SingleResponseEndpoint singleResponseEndpoint = singleResponseEndpointMap.get(singlResponseEndpointId);

                if (singleResponseEndpoint == null) {
                    List<HttpHeader> httpHeaders = new ArrayList<>();
                    singleResponseEndpoint = new SingleResponseEndpoint(
                            singlResponseEndpointId,
                            HttpMethod.valueOf(sqlRowSet.getString("HttpMethod")),
                            httpHeaders,
                            sqlRowSet.getString("Response"));
                    singleResponseEndpointMap.put(singlResponseEndpointId, singleResponseEndpoint);
                }

                singleResponseEndpoint.getHttpHeaders().add(new HttpHeader(httpHeaderName, httpHeaderValue));
            }
            return new ArrayList<>(singleResponseEndpointMap.values());
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Could not read all Single Response Endpoints from database, message: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void update(SingleResponseEndpoint singleResponseEndpoint) throws DatabaseException {
        Integer singlResponseEndpointId = singleResponseEndpoint.getId();
        if (singlResponseEndpointId == null) {
            log.warn("Cannot update Single Response Endpoint with null id");
        } else {
            try {
                jdbcTemplate.update("UPDATE voip.SingleResponseEndpoint SET HttpMethod = ?, Response = ?",
                        singleResponseEndpoint.getHttpMethod().toString(), singleResponseEndpoint.getResponse());
                jdbcTemplate.update("DELETE FROM voip.EndpointHeader WHERE EndpointId = ?", singlResponseEndpointId);
                singleResponseEndpoint.getHttpHeaders().forEach(e -> insertHeaderToDb(singlResponseEndpointId, e));
            } catch (Exception e) {
                log.error(String.format("Error while trying to save Single Response Endpoint with id %s to database, rolling back. Message: %s", singlResponseEndpointId, e.getMessage()));
                throw new DatabaseException(e.getMessage());
            }
        }
    }

    private void insertHeaderToDb(Number singlResponseEndpointId, HttpHeader header) {
        jdbcTemplate.update("insert into voip.EndpointHeader(EndpointId, Name, Value) VALUES(?,?,?)", singlResponseEndpointId, header.getName(), header.getValue());
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void remove(Integer singlResponseEndpointId) throws DatabaseException {
        try {
            jdbcTemplate.update("DELETE FROM voip.EndpointHeader WHERE EndpointId = ?", singlResponseEndpointId);
            jdbcTemplate.update("DELETE FROM voip.SingleResponseEndpoint WHERE Id = ?", singlResponseEndpointId);
        } catch (Exception e) {
            log.error(String.format("Error while trying to remove Single Response Endpoint with id %s, message: %s", singlResponseEndpointId, e.getMessage()));
            throw new DatabaseException(e.getMessage());
        }
    }
}
