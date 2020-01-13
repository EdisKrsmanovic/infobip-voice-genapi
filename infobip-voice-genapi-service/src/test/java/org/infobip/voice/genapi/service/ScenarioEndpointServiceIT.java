package org.infobip.voice.genapi.service;

import org.infobip.voice.genapi.Application;
import org.infobip.voice.genapi.TestConfiguration;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.model.EndpointResponse;
import org.infobip.voice.genapi.model.HttpHeader;
import org.infobip.voice.genapi.model.ScenarioEndpoint;
import org.infobip.voice.genapi.provider.ScenarioEndpointProvider;
import org.infobip.voice.genapi.validator.EndpointValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class ScenarioEndpointServiceIT {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScenarioEndpointService scenarioEndpointService;

    @Autowired
    private ScenarioEndpointProvider scenarioEndpointProvider;

    @SpyBean
    private EndpointValidator endpointValidator;

    @Before
    public void beforeEveryTest() {
        jdbcTemplate.update("DELETE FROM voip.EndpointHeader");
        jdbcTemplate.update("DELETE FROM voip.EndpointResponse");
        jdbcTemplate.update("DELETE FROM voip.ScenarioEndpoint");
    }

    @After
    public void afterEveryTest() {
        jdbcTemplate.update("DELETE FROM voip.EndpointHeader");
        jdbcTemplate.update("DELETE FROM voip.EndpointResponse");
        jdbcTemplate.update("DELETE FROM voip.ScenarioEndpoint");
    }

    @Test
    public void createHttpEndpointSavesHeadersInAnotherTable() {
        Integer numberOfHeadersBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM voip.EndpointHeader", Integer.class);
        scenarioEndpointService.createEndpoint(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));
        Integer numberOfHeadersAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM voip.EndpointHeader", Integer.class);
        assertThat(numberOfHeadersBefore).isEqualTo(numberOfHeadersAfter - 3);
    }

    @Test
    public void nullHttpMethodReturnsCorrectResponse() {
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointService.createEndpoint(new ScenarioEndpoint(null, null, givenHttpHeaders(), new ArrayList<>()));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("method");
    }

    @Test
    public void invalidHeadersReturnsCorrectResponse() {
        List<HttpHeader> headers = givenHttpHeaders();
        headers.get(0).setName("");
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointService.createEndpoint(new ScenarioEndpoint(null, HttpMethod.GET, headers, new ArrayList<>()));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("name");
    }

    @Test
    public void invalidBodyReturnsCorrectResponse() {
        List<EndpointResponse> endpointResponses = List.of(new EndpointResponse("invalid"));
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointService.createEndpoint(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), endpointResponses));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("json");
    }

    @Test
    public void getByIdReturns200IfFound() throws DatabaseException {
        Integer httpEndpointId = scenarioEndpointProvider.put(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));

        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointService.getById(httpEndpointId);

        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void getByIdReturns404IfNotFound() {
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointService.getById(0);

        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void createHttpResponseAddsResponseToList() {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointService.createEndpoint(scenarioEndpoint);

        scenarioEndpointService.createScenarioEndpointResponse(scenarioEndpoint.getId(), new EndpointResponse("{response}"));

        assertThat(scenarioEndpoint.getEndpointResponses().size()).isEqualTo(1);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createScenarioEndpointResponseThrowsInvalidBody() {
        ScenarioEndpoint httpEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointService.createEndpoint(httpEndpoint);

        scenarioEndpointService.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{response1}"));
    }

    @Test
    public void serviceReturnsResponsesCyclically() {
        ScenarioEndpoint httpEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointService.createEndpoint(httpEndpoint);

        scenarioEndpointService.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 1}"));
        scenarioEndpointService.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 2}"));
        scenarioEndpointService.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 3}"));

        assertThat(scenarioEndpointService.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 1}");
        assertThat(scenarioEndpointService.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 2}");
        assertThat(scenarioEndpointService.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 3}");
        assertThat(scenarioEndpointService.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 1}");
        assertThat(scenarioEndpointService.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 2}");
        assertThat(scenarioEndpointService.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 3}");
    }

    @Test
    public void createEndpointUsesValidatorThatChecksEveryResponse() {

        List<EndpointResponse> endpointResponses = List.of(
                new EndpointResponse("{}"),
                new EndpointResponse("{}"),
                new EndpointResponse("invalid"));

        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, Collections.emptyList(), endpointResponses);
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointService.createEndpoint(scenarioEndpoint);
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}