package org.infobip.voice.api;

import org.infobip.voice.Application;
import org.infobip.voice.exception.DatabaseException;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.model.HttpHeader;
import org.infobip.voice.repository.HttpEndpointRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class HttpEndpointRepositoryIT {

    @Autowired
    private HttpEndpointRepository httpEndpointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    public void getAllTest() throws DatabaseException {
        httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));
        List<HttpEndpoint> allEndpoints = httpEndpointRepository.getAll();
        assertThat(allEndpoints.size()).isEqualTo(2);
    }

    @Test
    public void getByIdTest() {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withSchemaName("voip").withTableName("HttpEndpoint").usingGeneratedKeyColumns("Id");
        Map<String, Object> parameters = Map.of(
                "HttpMethod", "GET",
                "Body", "{body}");
        Number httpEndpointId = jdbcInsert.executeAndReturnKey(parameters);

        HttpEndpoint httpEndpoint = httpEndpointRepository.getById(httpEndpointId.intValue());

        assertThat(httpEndpoint).isNotNull();
    }

    @Test
    public void updateByIdTest() throws DatabaseException {
        HttpEndpoint httpEndpoint = new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{body}");
        Integer httpEndpointId = httpEndpointRepository.save(httpEndpoint);

        httpEndpoint.setId(httpEndpointId);
        httpEndpoint.setBody("{newBody}");

        List<HttpHeader> httpHeaders = new ArrayList<>();
        httpHeaders.add(new HttpHeader("Novi", "Header"));
        httpEndpoint.setHttpHeaders(httpHeaders);

        httpEndpointRepository.update(httpEndpoint);

        httpEndpoint = httpEndpointRepository.getById(httpEndpointId);

        assertThat(httpEndpoint.getBody()).isEqualTo("{newBody}");
        assertThat(httpEndpoint.getHttpHeaders().get(0).getName()).isEqualTo("Novi");
        assertThat(httpEndpoint.getHttpHeaders().size()).isEqualTo(1);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}
