package org.infobip.voice.genapi.repository;

import org.infobip.voice.genapi.Application;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.model.EndpointResponse;
import org.infobip.voice.genapi.model.HttpHeader;
import org.infobip.voice.genapi.model.HttpMethod;
import org.infobip.voice.genapi.model.SingleResponseEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class SingleResponseEndpointRepositoryIT {

    @Autowired
    private SingleResponseEndpointRepository singleResponseEndpointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    public void getAllTest() throws DatabaseException {
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));
        List<SingleResponseEndpoint> allEndpoints = singleResponseEndpointRepository.getAll();
        assertThat(allEndpoints.size()).isEqualTo(2);
        assertThat(allEndpoints.get(0).getHttpHeaders().size()).isEqualTo(3);
        assertThat(allEndpoints.get(1).getHttpHeaders().size()).isEqualTo(3);
    }

    @Test
    public void getByIdTest() throws DatabaseException {
        Integer singleResponseEndpointId = singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{body}"));

        SingleResponseEndpoint singleResponseEndpoint = singleResponseEndpointRepository.getById(singleResponseEndpointId);

        assertThat(singleResponseEndpoint).isNotNull();
        assertThat(singleResponseEndpoint.getHttpHeaders().size()).isEqualTo(3);
    }

    @Test
    public void updateByIdTest() throws DatabaseException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{body}");

        singleResponseEndpointRepository.save(singleResponseEndpoint);
        Integer singleResponseEndpointId = singleResponseEndpointRepository.save(singleResponseEndpoint);

        singleResponseEndpoint.setId(singleResponseEndpointId);
        singleResponseEndpoint.setResponse(new EndpointResponse("{newBody}"));

        List<HttpHeader> httpHeaders = new ArrayList<>();
        httpHeaders.add(new HttpHeader("Novi", "Header"));
        singleResponseEndpoint.setHttpHeaders(httpHeaders);

        singleResponseEndpointRepository.update(singleResponseEndpoint);

        List<SingleResponseEndpoint> singleResponseEndpoints = singleResponseEndpointRepository.getAll();

        SingleResponseEndpoint untouchedSingleResponseEndpoint = singleResponseEndpoints.get(0);
        SingleResponseEndpoint updatedSingleResponseEndpoint = singleResponseEndpoints.get(1);

        assertThat(updatedSingleResponseEndpoint.getResponse().getBody()).isEqualTo("{newBody}");
        assertThat(updatedSingleResponseEndpoint.getHttpHeaders().get(0).getName()).isEqualTo("Novi");
        assertThat(updatedSingleResponseEndpoint.getHttpHeaders().size()).isEqualTo(1);

        assertThat(untouchedSingleResponseEndpoint.getResponse().getBody()).isEqualTo("{body}");
        assertThat(untouchedSingleResponseEndpoint.getHttpHeaders().get(0).getName()).isEqualTo("Accept");
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateWithNullId() throws DatabaseException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{body}");
        singleResponseEndpointRepository.update(singleResponseEndpoint);
    }

    @Test
    public void getAllWithNoHeadersTest() throws DatabaseException {
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.GET, Collections.emptyList(), "{}"));
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.POST, Collections.emptyList(), "{\"name\":\"value\"}"));

        List<SingleResponseEndpoint> singleResponseEndpoints = singleResponseEndpointRepository.getAll();

        assertThat(singleResponseEndpoints.size()).isEqualTo(2);
    }

    @Test
    public void getByIdWithNoHeadersTest() throws DatabaseException {
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.GET, Collections.emptyList(), "{}"));
        Integer singleResponseEndpointId = singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.POST, Collections.emptyList(), "{\"name\":\"value\"}"));

        SingleResponseEndpoint singleResponseEndpoint = singleResponseEndpointRepository.getById(singleResponseEndpointId);

        assertThat(singleResponseEndpoint).isNotNull();
        assertThat(singleResponseEndpoint.getId()).isEqualTo(singleResponseEndpointId);
        assertThat(singleResponseEndpoint.getHttpHeaders().size()).isEqualTo(0);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}
