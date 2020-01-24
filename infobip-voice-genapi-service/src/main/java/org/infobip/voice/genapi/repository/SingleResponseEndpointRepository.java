package org.infobip.voice.genapi.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.connector.model.HttpMethod;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.connector.model.HttpHeader;
import org.infobip.voice.genapi.connector.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.repository.mapper.SingleResponseEndpointRowMapper;
import org.infobip.voice.genapi.validator.EndpointValidator;
import org.springframework.dao.EmptyResultDataAccessException;
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

    private EndpointValidator endpointValidator;

    @Transactional(rollbackFor = DatabaseException.class)
    public Integer save(SingleResponseEndpoint singleResponseEndpoint) throws DatabaseException {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("SingleResponseEndpoint").usingGeneratedKeyColumns("Id");

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
            return jdbcTemplate.queryForObject(String.format("select * from SingleResponseEndpoint sre left join EndpointHeader eh on eh.EndpointId=sre.Id AND EndpointType = 'SingleResponse' where sre.id=%d", id), new SingleResponseEndpointRowMapper());
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            log.warn(String.format("Could not read Single Endpoint with id %s from database, message: %s", id, e.getMessage()));
        }
        return null;
    }

    public List<SingleResponseEndpoint> getAll() {
        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from SingleResponseEndpoint sre left join EndpointHeader eh on eh.EndpointId=sre.Id AND EndpointType = 'SingleResponse'");
            Map<Integer, SingleResponseEndpoint> singleResponseEndpointMap = new HashMap<>();

            while (sqlRowSet.next()) {
                int singleResponseEndpointId = sqlRowSet.getInt("Id");
                String httpHeaderName = sqlRowSet.getString("Name");
                String httpHeaderValue = sqlRowSet.getString("Value");

                SingleResponseEndpoint singleResponseEndpoint = singleResponseEndpointMap.get(singleResponseEndpointId);

                if (singleResponseEndpoint == null) {
                    singleResponseEndpoint = new SingleResponseEndpoint(
                            singleResponseEndpointId,
                            HttpMethod.valueOf(sqlRowSet.getString("HttpMethod")),
                            new ArrayList<>(),
                            sqlRowSet.getString("Response"));
                    singleResponseEndpointMap.put(singleResponseEndpointId, singleResponseEndpoint);
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
        Integer singleResponseEndpointId = singleResponseEndpoint.getId();
        endpointValidator.validate(singleResponseEndpoint, SingleResponseEndpoint.UpdateValidation.class);
            try {
                jdbcTemplate.update("UPDATE SingleResponseEndpoint SET HttpMethod = ?, Response = ?",
                        singleResponseEndpoint.getHttpMethod().toString(), singleResponseEndpoint.getResponse().getBody());
                jdbcTemplate.update("DELETE FROM EndpointHeader WHERE EndpointId = ? AND EndpointType = 'SingleResponse'", singleResponseEndpointId);
                singleResponseEndpoint.getHttpHeaders().forEach(e -> insertHeaderToDb(singleResponseEndpointId, e));
            } catch (Exception e) {
                log.error(String.format("Error while trying to save Single Response Endpoint with id %s to database, rolling back. Message: %s", singleResponseEndpointId, e.getMessage()));
                throw new DatabaseException(e.getMessage());
            }
    }

    private void insertHeaderToDb(Number singleResponseEndpointId, HttpHeader header) {
        jdbcTemplate.update("insert into EndpointHeader(EndpointId, Name, Value, EndpointType) VALUES(?,?,?,'SingleResponse')", singleResponseEndpointId, header.getName(), header.getValue());
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void remove(Integer singleResponseEndpointId) throws DatabaseException {
        try {
            jdbcTemplate.update("DELETE FROM EndpointHeader WHERE EndpointId = ? AND EndpointType = 'SingleResponse'", singleResponseEndpointId);
            jdbcTemplate.update("DELETE FROM SingleResponseEndpoint WHERE Id = ?", singleResponseEndpointId);
        } catch (Exception e) {
            log.error(String.format("Error while trying to remove Single Response Endpoint with id %s, message: %s", singleResponseEndpointId, e.getMessage()));
            throw new DatabaseException(e.getMessage());
        }
    }
}
