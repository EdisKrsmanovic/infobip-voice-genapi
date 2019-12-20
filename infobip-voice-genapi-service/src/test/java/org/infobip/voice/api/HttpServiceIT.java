package org.infobip.voice.api;

import org.infobip.voice.Application;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.model.HttpHeader;
import org.infobip.voice.provider.HttpEndpointProvider;
import org.infobip.voice.service.HttpService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class HttpServiceIT {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HttpService httpService;

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
        httpService.createHttpEndpoint(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{\"body\": \"bla\"}"));
        Integer numberOfHeadersAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM voip.HttpHeaders", Integer.class);
        assertThat(numberOfHeadersBefore).isEqualTo(numberOfHeadersAfter - 3);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createHttpEndpointShouldThrowIfIfHttpMethodIsNull() {
        httpService.createHttpEndpoint(new HttpEndpoint(null, null, givenHttpHeaders(), "{\"valid\"}: \"body\""));
    }

    @Test(expected = ConstraintViolationException.class)
    public void createHttpEndpointShouldThrowIfIfHeadersAreInvalid() {
        List<HttpHeader> headers = givenHttpHeaders();
        headers.get(0).setName("");
        httpService.createHttpEndpoint(new HttpEndpoint(null, null, headers, "{\"valid\"}: \"body\""));
    }

    @Test(expected = ConstraintViolationException.class)
    public void createHttpEndpointShouldThrowIfIBodyIsInvalid() {
        httpService.createHttpEndpoint(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "invalid Body Example"));
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}
