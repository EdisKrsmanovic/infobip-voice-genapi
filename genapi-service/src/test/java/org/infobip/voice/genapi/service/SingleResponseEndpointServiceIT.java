package edis.krsmanovic.genapi.service;

import edis.krsmanovic.genapi.Application;
import edis.krsmanovic.genapi.exception.DatabaseException;
import edis.krsmanovic.genapi.model.GenApiResponse;
import edis.krsmanovic.genapi.model.HttpHeader;
import edis.krsmanovic.genapi.model.HttpMethod;
import edis.krsmanovic.genapi.model.SingleResponseEndpoint;
import edis.krsmanovic.genapi.provider.SingleResponseEndpointProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class SingleResponseEndpointServiceIT {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SingleResponseEndpointServiceImpl singleResponseEndpointService;

    @Autowired
    private SingleResponseEndpointProvider singleResponseEndpointProvider;

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
