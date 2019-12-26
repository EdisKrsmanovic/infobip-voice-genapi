package org.infobip.voice.genapi.service;

import org.infobip.voice.genapi.Application;
import org.infobip.voice.genapi.TestConfiguration;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.provider.HttpEndpointProvider;
import org.infobip.voice.genapi.model.HttpEndpoint;
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
public class HttpEndpointServiceIT {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HttpEndpointService httpEndpointService;

    @Autowired
    private HttpEndpointProvider httpEndpointProvider;

    @Before
    public void beforeEveryTest() {
        jdbcTemplate.update("DELETE FROM voip.HttpHeaders");
        jdbcTemplate.update("DELETE FROM voip.HttpEndpoint");
    }

    @After
    public void afterEveryTest() {
        jdbcTemplate.update("DELETE FROM voip.HttpHeaders");
        jdbcTemplate.update("DELETE FROM voip.HttpEndpoint");
    }

    @Test
    public void createHttpEndpointSavesHeadersInAnotherTable() {
        Integer numberOfHeadersBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM voip.HttpHeaders", Integer.class);
        httpEndpointService.createHttpEndpoint(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{\"body\": \"bla\"}"));
        Integer numberOfHeadersAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM voip.HttpHeaders", Integer.class);
        assertThat(numberOfHeadersBefore).isEqualTo(numberOfHeadersAfter - 3);
    }

    @Test
    public void nullHttpMethodReturnsCorrectResponse() {
        GenApiResponse<HttpEndpoint> httpEndpointGenApiResponse = httpEndpointService.createHttpEndpoint(new HttpEndpoint(null, null, givenHttpHeaders(), "{\"valid\"}: \"body\""));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("method");
    }

    @Test
    public void invalidHeadersReturnsCorrectResponse() {
        List<HttpHeader> headers = givenHttpHeaders();
        headers.get(0).setName("");
        GenApiResponse<HttpEndpoint> httpEndpointGenApiResponse = httpEndpointService.createHttpEndpoint(new HttpEndpoint(null, HttpMethod.GET, headers, "{\"valid\": \"body\"}"));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("name");
    }

    @Test
    public void invalidBodyReturnsCorrectResponse() {
        GenApiResponse<HttpEndpoint> httpEndpointGenApiResponse = httpEndpointService.createHttpEndpoint(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "invalid Body Example"));
        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(400);
        assertThat(httpEndpointGenApiResponse.getMessage().toLowerCase()).contains("body");
    }

    @Test
    public void getByIdReturns200IfFound() throws DatabaseException {
        Integer httpEndpointId = httpEndpointProvider.put(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{\"body\": \"bla\"}"));

        GenApiResponse<HttpEndpoint> httpEndpointGenApiResponse = httpEndpointService.getById(httpEndpointId);

        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void getByIdReturns404IfNotFound() {
        GenApiResponse<HttpEndpoint> httpEndpointGenApiResponse = httpEndpointService.getById(0);

        assertThat(httpEndpointGenApiResponse.getStatusCode()).isEqualTo(404);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}
