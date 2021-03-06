package edis.krsmanovic.genapi.service;

import edis.krsmanovic.genapi.Application;
import edis.krsmanovic.genapi.exception.DatabaseException;
import edis.krsmanovic.genapi.model.*;
import edis.krsmanovic.genapi.provider.ScenarioEndpointProvider;
import edis.krsmanovic.genapi.repository.ScenarioEndpointRepository;
import edis.krsmanovic.genapi.validator.EndpointValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ScenarioEndpointServiceImplIT {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScenarioEndpointServiceImpl scenarioEndpointServiceImpl;

    @Autowired
    private ScenarioEndpointRepository scenarioEndpointRepository;

    @Autowired
    private ScenarioEndpointProvider scenarioEndpointProvider;

    @SpyBean
    private EndpointValidator endpointValidator;

    @BeforeEach
    public void beforeEveryTest() {
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM EndpointResponse");
        jdbcTemplate.update("DELETE FROM ScenarioEndpoint");
    }

    @AfterEach
    public void afterEveryTest() {
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM EndpointResponse");
        jdbcTemplate.update("DELETE FROM ScenarioEndpoint");
    }

    @Test
    public void createHttpEndpointSavesHeadersInAnotherTable() {
        Integer numberOfHeadersBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EndpointHeader", Integer.class);
        scenarioEndpointServiceImpl.createEndpoint(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));
        Integer numberOfHeadersAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EndpointHeader", Integer.class);
        assertThat(numberOfHeadersBefore).isEqualTo(numberOfHeadersAfter - 3);
    }

    @Test
    public void nullHttpMethodReturnsCorrectResponse() {
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.createEndpoint(new ScenarioEndpoint(null, null, givenHttpHeaders(), new ArrayList<>()));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("method");
    }

    @Test
    public void invalidHeadersReturnsCorrectResponse() {
        List<HttpHeader> headers = givenHttpHeaders();
        headers.get(0).setName("");
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.createEndpoint(new ScenarioEndpoint(null, HttpMethod.GET, headers, new ArrayList<>()));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("name");
    }

    @Test
    public void invalidBodyReturnsCorrectResponse() {
        List<EndpointResponse> endpointResponses = List.of(new EndpointResponse("invalid"));
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.createEndpoint(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), endpointResponses));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("body");
    }

    @Test
    public void getByIdReturns200IfFound() throws DatabaseException {
        Integer httpEndpointId = scenarioEndpointProvider.put(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));

        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.getById(httpEndpointId);

        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void getByIdReturns404IfNotFound() {
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.getById(0);

        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void createHttpResponseAddsResponseToList() {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointServiceImpl.createEndpoint(scenarioEndpoint);

        scenarioEndpointServiceImpl.createScenarioEndpointResponse(scenarioEndpoint.getId(), new EndpointResponse("{\"response\": \"asd\"}"));

        assertThat(scenarioEndpoint.getEndpointResponses().size()).isEqualTo(1);
    }

    @Test
    public void createScenarioEndpointReturnsCorrectResponseOnInvalidBody() {
        ScenarioEndpoint httpEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointServiceImpl.createEndpoint(httpEndpoint);

        GenApiResponse<EndpointResponse> scenarioEndpointResponse = scenarioEndpointServiceImpl.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{response1}"));
        assertThat(scenarioEndpointResponse.getStatusCode()).isEqualTo(400);
    }

    @Test
    public void updatingNonExistingScenarioReturns404() throws DatabaseException {
//        Integer httpEndpointId = scenarioEndpointProvider.put(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));

        List<EndpointResponse> endpointResponses = List.of(new EndpointResponse("invalid"));
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.updateEndpoint(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), endpointResponses));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(404);
    }
    @Test

    public void updateWithInvalidBodyReturnsCorrectResponse() throws DatabaseException {
        Integer httpEndpointId = scenarioEndpointProvider.put(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));

        List<EndpointResponse> endpointResponses = List.of(new EndpointResponse("invalid"));
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.updateEndpoint(new ScenarioEndpoint(httpEndpointId, HttpMethod.GET, givenHttpHeaders(), endpointResponses));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
    }

    @Test
    public void serviceReturnsResponsesCyclically() {
        ScenarioEndpoint httpEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointServiceImpl.createEndpoint(httpEndpoint);

        scenarioEndpointServiceImpl.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 1}"));
        scenarioEndpointServiceImpl.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 2}"));
        scenarioEndpointServiceImpl.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 3}"));

        assertThat(scenarioEndpointServiceImpl.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 1}");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 2}");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 3}");
    }

    @Test
    public void serviceThrowsWhenEveryResponseHasBeenReturned() {
        ScenarioEndpoint httpEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointServiceImpl.createEndpoint(httpEndpoint);

        scenarioEndpointServiceImpl.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 1}"));
        scenarioEndpointServiceImpl.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 2}"));
        scenarioEndpointServiceImpl.createScenarioEndpointResponse(httpEndpoint.getId(), new EndpointResponse("{\"response1\": 3}"));

        assertThat(scenarioEndpointServiceImpl.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 1}");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 2}");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(httpEndpoint.getId()).getEntity().getBody()).isEqualTo("{\"response1\": 3}");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(httpEndpoint.getId()).getStatusCode()).isEqualTo(204);
    }

    @Test
    public void createEndpointUsesValidatorThatChecksEveryResponse() {

        List<EndpointResponse> endpointResponses = List.of(
                new EndpointResponse("{}"),
                new EndpointResponse("{}"),
                new EndpointResponse("invalid"));

        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, Collections.emptyList(), endpointResponses);
        GenApiResponse<ScenarioEndpoint> httpEndpointGenApiResponse = scenarioEndpointServiceImpl.createEndpoint(scenarioEndpoint);
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
    }

    @Test
    public void endpointResponsesAreSortedByOrdinalNumbers() throws DatabaseException {
        Integer scenarioEndpointId = scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), givenEndpointResponses()));

        scenarioEndpointProvider.reloadAll();

        assertThat(scenarioEndpointServiceImpl.getNextResponse(scenarioEndpointId).getEntity().getBody()).isEqualTo("{\"body\": \"This should be 1st\"");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(scenarioEndpointId).getEntity().getBody()).isEqualTo("{\"body\": \"This should be 2nd\"");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(scenarioEndpointId).getEntity().getBody()).isEqualTo("{\"body\": \"This should be 3rd\"");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(scenarioEndpointId).getEntity().getBody()).isEqualTo("{\"body\": \"This should be 4th\"");
        assertThat(scenarioEndpointServiceImpl.getNextResponse(scenarioEndpointId).getEntity().getBody()).isEqualTo("{\"body\": \"This should be 5th\"");
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(
                new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive")
        );
    }

    private List<EndpointResponse> givenEndpointResponses() {
        return List.of(
                new EndpointResponse("{\"body\": \"This should be 2nd\"", 2),
                new EndpointResponse("{\"body\": \"This should be 4th\"", 4),
                new EndpointResponse("{\"body\": \"This should be 3rd\"", 3),
                new EndpointResponse("{\"body\": \"This should be 5th\"", 5),
                new EndpointResponse("{\"body\": \"This should be 1st\"", 1)
        );
    }
}