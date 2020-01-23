package org.infobip.voice.genapi.service;

import org.infobip.voice.genapi.Application;
import org.infobip.voice.genapi.TestConfiguration;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.provider.SingleResponseEndpointProvider;
import org.infobip.voice.genapi.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.model.HttpHeader;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class SingleResponseEndpointServiceIT {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SingleResponseEndpointService singleResponseEndpointService;

    @Autowired
    private SingleResponseEndpointProvider singleResponseEndpointProvider;

    @Before
    public void beforeEveryTest() {
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM SingleResponseEndpoint");
    }

    @After
    public void afterEveryTest() {
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM SingleResponseEndpoint");
    }

    @Test
    public void createSingleResponseEndpointSavesHeadersInAnotherTable() {
        Integer numberOfHeadersBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EndpointHeader", Integer.class);
        singleResponseEndpointService.createEndpoint(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{\"body\": \"bla\"}"));
        Integer numberOfHeadersAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EndpointHeader", Integer.class);
        assertThat(numberOfHeadersBefore).isEqualTo(numberOfHeadersAfter - 3);
    }

    @Test
    public void nullHttpMethodReturnsCorrectResponse() {
        GenApiResponse<SingleResponseEndpoint> singleResponseEndpointGenApiResponse = singleResponseEndpointService.createEndpoint(new SingleResponseEndpoint(null, null, givenHttpHeaders(), "{\"valid\"}: \"body\""));
        assertThat(singleResponseEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(singleResponseEndpointGenApiResponse.getMessage().toLowerCase()).contains("method");
    }

    @Test
    public void invalidHeadersReturnsCorrectResponse() {
        List<HttpHeader> headers = givenHttpHeaders();
        headers.get(0).setName("");
        GenApiResponse<SingleResponseEndpoint> singleResponseEndpointGenApiResponse = singleResponseEndpointService.createEndpoint(new SingleResponseEndpoint(null, HttpMethod.GET, headers, "{\"valid\": \"body\"}"));
        assertThat(singleResponseEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(singleResponseEndpointGenApiResponse.getMessage().toLowerCase()).contains("name");
    }

    @Test
    public void invalidBodyReturnsCorrectResponse() {
        GenApiResponse<SingleResponseEndpoint> singleResponseEndpointGenApiResponse = singleResponseEndpointService.createEndpoint(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "invalid Body Example"));
        assertThat(singleResponseEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(singleResponseEndpointGenApiResponse.getMessage().toLowerCase()).contains("body");
    }

    @Test
    public void getByIdReturns200IfFound() throws DatabaseException {
        Integer singleResponseEndpointId = singleResponseEndpointProvider.put(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{\"body\": \"bla\"}"));

        GenApiResponse<SingleResponseEndpoint> singleResponseEndpointGenApiResponse = singleResponseEndpointService.getById(singleResponseEndpointId);

        assertThat(singleResponseEndpointGenApiResponse.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void getByIdReturns404IfNotFound() {
        GenApiResponse<SingleResponseEndpoint> singleResponseEndpointGenApiResponse = singleResponseEndpointService.getById(0);

        assertThat(singleResponseEndpointGenApiResponse.getStatusCode()).isEqualTo(404);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}
