package org.infobip.voice.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.exception.DatabaseException;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.model.HttpHeader;
import org.infobip.voice.repository.mapper.HttpEndpointRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            return httpEndpointId.intValue();
        } catch (Exception e) {
            log.error("Error while trying to save HttpEndpoint to database, rolling back");
            throw new DatabaseException(e.getMessage());
        }
    }

    public HttpEndpoint getById(Integer id) {
        try {
            return jdbcTemplate.queryForObject(String.format("select * from voip.HttpEndpoint where id=%d", id), new HttpEndpointRowMapper(jdbcTemplate));
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            log.warn(String.format("Could not read HttpEndpoint with id %s from database", id));
        }
        return null;
    }

    public List<HttpEndpoint> getAll() {
        try {
            return jdbcTemplate.query("select * from voip.HttpEndpoint", new HttpEndpointRowMapper(jdbcTemplate));
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
}
