package org.infobip.voice.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.exception.DatabaseException;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.model.HttpHeader;
import org.infobip.voice.repository.mapper.HttpEndpointRowMapper;
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
public class HttpEndpointRepository {

    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = DatabaseException.class)
    public Integer save(HttpEndpoint httpEndpoint) throws DatabaseException {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withSchemaName("voip").withTableName("HttpEndpoint").usingGeneratedKeyColumns("Id");

        Map<String, Object> parameters = Map.of(
                "HttpMethod", httpEndpoint.getHttpMethod().toString(),
                "Body", httpEndpoint.getBody());
        try {
            Number httpEndpointId = jdbcInsert.executeAndReturnKey(parameters);
            httpEndpoint.getHttpHeaders().forEach(e -> insertHeaderToDb(httpEndpointId, e));
            httpEndpoint.setId(httpEndpointId.intValue());
            return httpEndpointId.intValue();
        } catch (Exception e) {
            log.error("Error while trying to save HttpEndpoint to database, rolling back");
            throw new DatabaseException(e.getMessage());
        }
    }

    public HttpEndpoint getById(Integer id) {
        try {
            return jdbcTemplate.queryForObject(String.format("select * from voip.HttpEndpoint left join voip.HttpHeaders on HttpEndpointId=Id where id=%d", id), new HttpEndpointRowMapper());
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            log.warn(String.format("Could not read HttpEndpoint with id %s from database, message: %s", id, e.getMessage()));
        }
        return null;
    }

    public List<HttpEndpoint> getAll() {
        try {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from voip.HttpEndpoint left join voip.HttpHeaders on HttpEndpointId=Id");
            Map<Integer, HttpEndpoint> httpEndpointMap = new HashMap<>();

            while (sqlRowSet.next()) {
                int httpEndpointId = sqlRowSet.getInt("Id");
                String httpHeaderName = sqlRowSet.getString("Name");
                String httpHeaderValue = sqlRowSet.getString("Value");

                HttpEndpoint httpEndpoint = httpEndpointMap.get(httpEndpointId);

                if (httpEndpoint == null) {
                    List<HttpHeader> httpHeaders = new ArrayList<>();
                    httpEndpoint = new HttpEndpoint(
                            httpEndpointId,
                            HttpMethod.valueOf(sqlRowSet.getString("HttpMethod")),
                            httpHeaders,
                            sqlRowSet.getString("Body"));
                    httpEndpointMap.put(httpEndpointId, httpEndpoint);
                }

                httpEndpoint.getHttpHeaders().add(new HttpHeader(httpHeaderName, httpHeaderValue));
            }
            return new ArrayList<>(httpEndpointMap.values());
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            log.warn("Could not read all HttpEndpoints from database, message: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void update(HttpEndpoint httpEndpoint) throws DatabaseException {
        Integer httpEndpointId = httpEndpoint.getId();
        if (httpEndpointId == null) {
            log.warn("Cannot update HttpEndpoint with null id");
        } else {
            try {
                jdbcTemplate.update("UPDATE voip.HttpEndpoint SET HttpMethod = ?, Body = ?",
                        httpEndpoint.getHttpMethod().toString(), httpEndpoint.getBody());
                jdbcTemplate.update("DELETE FROM voip.HttpHeaders WHERE HttpEndpointId = ?", httpEndpointId);
                httpEndpoint.getHttpHeaders().forEach(e -> insertHeaderToDb(httpEndpointId, e));
            } catch (Exception e) {
                log.error(String.format("Error while trying to save HttpEndpoint with id %s to database, rolling back. Message: %s", httpEndpointId, e.getMessage()));
                throw new DatabaseException(e.getMessage());
            }
        }
    }

    private void insertHeaderToDb(Number httpEndpointId, HttpHeader header) {
        jdbcTemplate.update("insert into voip.HttpHeaders VALUES(?,?,?)", httpEndpointId, header.getName(), header.getValue());
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void remove(Integer httpEndpointId) throws DatabaseException {
        try {
            jdbcTemplate.update("DELETE FROM voip.HttpHeaders WHERE HttpEndpointId = ?", httpEndpointId);
            jdbcTemplate.update("DELETE FROM voip.HttpEndpoint WHERE Id = ?", httpEndpointId);
        } catch (Exception e) {
            log.error(String.format("Error while trying to remove httpendpoint with id %s, message: %s", httpEndpointId, e.getMessage()));
            throw new DatabaseException(e.getMessage());
        }
    }
}
