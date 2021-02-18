package edis.krsmanovic.genapi.repository;

import edis.krsmanovic.genapi.Application;
import edis.krsmanovic.genapi.exception.DatabaseException;
import edis.krsmanovic.genapi.model.EndpointResponse;
import edis.krsmanovic.genapi.model.HttpHeader;
import edis.krsmanovic.genapi.model.HttpMethod;
import edis.krsmanovic.genapi.model.SingleResponseEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class SingleResponseEndpointRepositoryIT {

    @Autowired
    private SingleResponseEndpointRepository singleResponseEndpointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEveryTest() {
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM SingleResponseEndpoint");
    }

    @AfterEach
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
        assertThat(untouchedSingleResponseEndpoint.getHttpHeaders().size()).isEqualTo(3);
    }

    @Test
    public void updateWithNullId() throws DatabaseException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{body}");
        assertThrows(ConstraintViolationException.class, () -> singleResponseEndpointRepository.update(singleResponseEndpoint));
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
