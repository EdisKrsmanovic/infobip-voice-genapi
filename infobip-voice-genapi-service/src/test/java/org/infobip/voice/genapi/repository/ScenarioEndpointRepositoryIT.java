package org.infobip.voice.genapi.repository;

import org.infobip.voice.genapi.Application;
import org.infobip.voice.genapi.TestConfiguration;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.model.EndpointResponse;
import org.infobip.voice.genapi.model.HttpHeader;
import org.infobip.voice.genapi.model.ScenarioEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class ScenarioEndpointRepositoryIT {

    @Autowired
    private ScenarioEndpointRepository scenarioEndpointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    public void getAllTest() throws DatabaseException {
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>()));
        List<ScenarioEndpoint> allEndpoints = scenarioEndpointRepository.getAll();
        assertThat(allEndpoints.size()).isEqualTo(2);
        assertThat(allEndpoints.get(0).getHttpHeaders().size()).isEqualTo(3);
        assertThat(allEndpoints.get(1).getHttpHeaders().size()).isEqualTo(3);
    }

    @Test
    public void getByIdTest() throws DatabaseException {
        Integer httpEndpointId = scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));

        ScenarioEndpoint scenarioEndpoint = scenarioEndpointRepository.getById(httpEndpointId);

        assertThat(scenarioEndpoint).isNotNull();
        assertThat(scenarioEndpoint.getHttpHeaders().size()).isEqualTo(3);
    }

    @Test
    public void updateByIdTest() throws DatabaseException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        Integer httpEndpointId = scenarioEndpointRepository.save(scenarioEndpoint);

        scenarioEndpoint.setId(httpEndpointId);

        List<HttpHeader> httpHeaders = new ArrayList<>();
        httpHeaders.add(new HttpHeader("Novi", "Header"));
        scenarioEndpoint.setHttpHeaders(httpHeaders);

        scenarioEndpointRepository.update(scenarioEndpoint);

        scenarioEndpoint = scenarioEndpointRepository.getById(httpEndpointId);

        assertThat(scenarioEndpoint.getHttpHeaders().get(0).getName()).isEqualTo("Novi");
        assertThat(scenarioEndpoint.getHttpHeaders().size()).isEqualTo(1);
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateWithNullId() throws DatabaseException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointRepository.update(scenarioEndpoint);
    }

    @Test
    public void getAllWithNoHeadersTest() throws DatabaseException {
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, Collections.emptyList(), new ArrayList<>()));
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.POST, Collections.emptyList(), new ArrayList<>()));

        List<ScenarioEndpoint> httpEndpoints = scenarioEndpointRepository.getAll();

        assertThat(httpEndpoints.size()).isEqualTo(2);
    }

    @Test
    public void getByIdWithNoHeadersTest() throws DatabaseException {
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, Collections.emptyList(), new ArrayList<>()));
        Integer scenarioEndpointId = scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.POST, Collections.emptyList(), new ArrayList<>()));

        ScenarioEndpoint scenarioEndpoint = scenarioEndpointRepository.getById(scenarioEndpointId);

        assertThat(scenarioEndpoint).isNotNull();
        assertThat(scenarioEndpoint.getId()).isEqualTo(scenarioEndpointId);
        assertThat(scenarioEndpoint.getHttpHeaders().size()).isEqualTo(0);
    }

    @Test
    public void savingResponsesSavesCorrectly() throws DatabaseException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        EndpointResponse endpointResponse1 = new EndpointResponse("{response1}", 1);
        EndpointResponse endpointResponse2 = new EndpointResponse("{response2}", 2);

        scenarioEndpointRepository.save(scenarioEndpoint);
        scenarioEndpointRepository.save(scenarioEndpoint.getId(), endpointResponse1);
        scenarioEndpointRepository.save(scenarioEndpoint.getId(), endpointResponse2);

        ScenarioEndpoint scenarioEndpoint2 = scenarioEndpointRepository.getById(scenarioEndpoint.getId());

        assertThat(scenarioEndpoint2.getEndpointResponses().size()).isEqualTo(2);
    }

    @Test
    public void gettingEndpointReturnsResponsesInCorrectOrder() throws DatabaseException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        EndpointResponse endpointResponse1 = new EndpointResponse("{response1}", 2);
        EndpointResponse endpointResponse2 = new EndpointResponse("{response2}", 11);

        scenarioEndpointRepository.save(scenarioEndpoint);
        scenarioEndpointRepository.save(scenarioEndpoint.getId(), endpointResponse1);
        scenarioEndpointRepository.save(scenarioEndpoint.getId(), endpointResponse2);

        ScenarioEndpoint scenarioEndpoint2 = scenarioEndpointRepository.getById(scenarioEndpoint.getId());

        List<EndpointResponse> responses = scenarioEndpoint2.getEndpointResponses();

        assertThat(responses.size()).isEqualTo(2);
        assertThat(responses.get(0).getOrdinalNumber()).isLessThan(responses.get(1).getOrdinalNumber());
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}