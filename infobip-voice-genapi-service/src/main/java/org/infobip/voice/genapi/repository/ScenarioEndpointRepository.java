package org.infobip.voice.genapi.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.model.EndpointResponse;
import org.infobip.voice.genapi.model.HttpHeader;
import org.infobip.voice.genapi.model.ScenarioEndpoint;
import org.infobip.voice.genapi.repository.mapper.ScenarioEndpointRowMapper;
import org.infobip.voice.genapi.validator.EndpointValidator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Repository
@AllArgsConstructor
public class ScenarioEndpointRepository {

    private JdbcTemplate jdbcTemplate;

    private EndpointValidator endpointValidator;

    @Transactional(rollbackFor = DatabaseException.class)
    public Integer save(ScenarioEndpoint scenarioEndpoint) throws DatabaseException {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withSchemaName("voip").withTableName("ScenarioEndpoint").usingGeneratedKeyColumns("Id");

        Map<String, Object> parameters = Map.of(
                "HttpMethod", scenarioEndpoint.getHttpMethod().toString());
        try {
            Number scenarioEndpointId = jdbcInsert.executeAndReturnKey(parameters);
            scenarioEndpoint.getHttpHeaders().forEach(e -> insertHeaderToDb(scenarioEndpointId, e));
            scenarioEndpoint.getEndpointResponses().forEach(e -> insertResponseToDb(scenarioEndpointId, e));
            scenarioEndpoint.setId(scenarioEndpointId.intValue());
            return scenarioEndpointId.intValue();
        } catch (Exception e) {
            log.error("Error while trying to save Scenario Endpoint to database, rolling back");
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }

    public ScenarioEndpoint getById(Integer id) {
        try {
            ScenarioEndpoint scenarioEndpoint = jdbcTemplate.queryForObject(String.format("select * from voip.ScenarioEndpoint s left join voip.EndpointHeader e on e.EndpointId=s.Id AND EndpointType = 'Scenario' where s.id=%d", id), new ScenarioEndpointRowMapper());

            List<EndpointResponse> endpointResponses = jdbcTemplate.query(String.format("Select Body, OrdinalNumber from voip.EndpointResponse where EndpointId=%d order by OrdinalNumber asc", id), new BeanPropertyRowMapper<>(EndpointResponse.class));

            if (scenarioEndpoint == null) return null;

            scenarioEndpoint.setEndpointResponses(endpointResponses);
            return scenarioEndpoint;
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            log.warn(String.format("Could not read Scenario Endpoint with id %s from database, message: %s", id, e.getMessage()));
        }
        return null;
    }

    public List<ScenarioEndpoint> getAll() {
        try {
            Map<Integer, ScenarioEndpoint> scenarioEndpointMap = new HashMap<>();
            readHeaders(scenarioEndpointMap);
            readResponses(scenarioEndpointMap);
            return new ArrayList<>(scenarioEndpointMap.values());
        } catch (EmptyResultDataAccessException e) {
            log.warn("No results found");
        } catch (Exception e) {
            log.warn("Could not read all Scenario Endpoints from database, message: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void update(ScenarioEndpoint scenarioEndpoint) throws DatabaseException {
        endpointValidator.validate(scenarioEndpoint, ScenarioEndpoint.UpdateValidation.class);
        Integer scenarioEndpointId = scenarioEndpoint.getId();
        try {
            jdbcTemplate.update("UPDATE voip.ScenarioEndpoint SET HttpMethod = ?",
                    scenarioEndpoint.getHttpMethod().toString());

            //Consider doing this in provider instead
            if (shouldUpdateHeaders(scenarioEndpoint, scenarioEndpointId)) {
                jdbcTemplate.update("DELETE FROM voip.EndpointHeader WHERE EndpointId = ? AND EndpointType = 'Scenario'", scenarioEndpointId);
                scenarioEndpoint.getHttpHeaders().forEach(e -> insertHeaderToDb(scenarioEndpointId, e));
            }

            if (shouldUpdateResponses(scenarioEndpoint, scenarioEndpointId)) {
                jdbcTemplate.update("DELETE FROM voip.EndpointResponse WHERE EndpointId = ?", scenarioEndpointId);
                scenarioEndpoint.getEndpointResponses().forEach(e -> insertResponseToDb(scenarioEndpointId, e));
            }

        } catch (Exception e) {
            log.error(String.format("Error while trying to save Scenario Endpoint with id %s to database, rolling back. Message: %s", scenarioEndpointId, e.getMessage()));
            throw new DatabaseException(e.getMessage());
        }
    }

    private boolean shouldUpdateResponses(ScenarioEndpoint scenarioEndpoint, Integer scenarioEndpointId) {
        boolean responsesMismatch = false;
        List<EndpointResponse> endpointResponses = jdbcTemplate.query(String.format("Select Body, OrdinalNumber from voip.EndpointResponse where EndpointId=%d", scenarioEndpointId), new BeanPropertyRowMapper<>(EndpointResponse.class));
        if (endpointResponses.size() != scenarioEndpoint.getEndpointResponses().size()) responsesMismatch = true;
        else {
            if (scenarioEndpoint.getEndpointResponses().stream().anyMatch(e -> !endpointResponses.contains(e)))
                responsesMismatch = true;
        }
        return responsesMismatch;
    }

    private boolean shouldUpdateHeaders(ScenarioEndpoint scenarioEndpoint, Integer scenarioEndpointId) {
        boolean headersMismatch = false;
        List<HttpHeader> persistedEndpointHeaders = jdbcTemplate.query(String.format("Select Name, Value from voip.EndpointHeader where EndpointId=%d AND EndpointType = 'Scenario'", scenarioEndpointId), new BeanPropertyRowMapper<>(HttpHeader.class));
        if (persistedEndpointHeaders.size() != scenarioEndpoint.getHttpHeaders().size()) headersMismatch = true;
        else {
            if (scenarioEndpoint.getHttpHeaders().stream().anyMatch(e -> !persistedEndpointHeaders.contains(e)))
                headersMismatch = true;
        }
        return headersMismatch;
    }

    private void insertHeaderToDb(Number scenarioEndpointId, HttpHeader header) {
        jdbcTemplate.update("insert into voip.EndpointHeader(EndpointId, Name, Value, EndpointType) VALUES(?,?,?,'Scenario')", scenarioEndpointId, header.getName(), header.getValue());
    }

    private void insertResponseToDb(Number scenarioEndpointId, EndpointResponse endpointResponse) {
        jdbcTemplate.update("insert into voip.EndpointResponse(EndpointId, Body, OrdinalNumber) VALUES(?,?,?)", scenarioEndpointId, endpointResponse.getBody(), endpointResponse.getOrdinalNumber());
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void remove(Integer scenarioEndpointId) throws DatabaseException {
        try {
            jdbcTemplate.update("DELETE FROM voip.EndpointHeader WHERE EndpointId = ? AND EndpointType = 'Scenario'", scenarioEndpointId);
            jdbcTemplate.update("DELETE FROM voip.EndpointResponse WHERE Id = ?", scenarioEndpointId);
            jdbcTemplate.update("DELETE FROM voip.ScenarioEndpoint WHERE Id = ?", scenarioEndpointId);
        } catch (Exception e) {
            log.error(String.format("Error while trying to remove Scenario Endpoint with id %s, message: %s", scenarioEndpointId, e.getMessage()));
            throw new DatabaseException(e.getMessage());
        }
    }

    public void save(Integer scenarioEndpointId, EndpointResponse endpointResponse) {
        insertResponseToDb(scenarioEndpointId, endpointResponse);
    }

    private void readHeaders(Map<Integer, ScenarioEndpoint> scenarioEndpointMap) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from voip.ScenarioEndpoint left join voip.EndpointHeader on ScenarioEndpoint.Id = EndpointHeader.EndpointId AND EndpointType = 'Scenario'");

        while (sqlRowSet.next()) {
            int scenarioEndpointId = sqlRowSet.getInt("Id");
            String httpHeaderName = sqlRowSet.getString("Name");
            String httpHeaderValue = sqlRowSet.getString("Value");

            ScenarioEndpoint scenarioEndpoint = scenarioEndpointMap.get(scenarioEndpointId);

            if (scenarioEndpoint == null) {
                scenarioEndpoint = new ScenarioEndpoint(
                        scenarioEndpointId,
                        HttpMethod.valueOf(sqlRowSet.getString("HttpMethod")),
                        new ArrayList<>(),
                        new ArrayList<>());
                scenarioEndpointMap.put(scenarioEndpointId, scenarioEndpoint);
            }

            scenarioEndpoint.getHttpHeaders().add(new HttpHeader(httpHeaderName, httpHeaderValue));
        }
    }

    private void readResponses(Map<Integer, ScenarioEndpoint> scenarioEndpointMap) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from voip.ScenarioEndpoint left join voip.EndpointResponse on ScenarioEndpoint.Id = EndpointResponse.EndpointId");
        while (sqlRowSet.next()) {
            int scenarioEndpointId = sqlRowSet.getInt("Id");
            String body = sqlRowSet.getString("Body");
            Integer ordinalNumber = sqlRowSet.getInt("OrdinalNumber");

            ScenarioEndpoint scenarioEndpoint = scenarioEndpointMap.get(scenarioEndpointId);

            scenarioEndpoint.getEndpointResponses().add(new EndpointResponse(body, ordinalNumber));
        }
    }
}