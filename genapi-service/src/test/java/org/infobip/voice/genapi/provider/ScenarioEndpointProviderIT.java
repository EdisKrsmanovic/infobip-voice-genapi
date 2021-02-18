package edis.krsmanovic.genapi.provider;

import edis.krsmanovic.genapi.Application;
import edis.krsmanovic.genapi.exception.DatabaseException;
import edis.krsmanovic.genapi.exception.HttpEndpointNotFoundException;
import edis.krsmanovic.genapi.model.HttpHeader;
import edis.krsmanovic.genapi.model.HttpMethod;
import edis.krsmanovic.genapi.model.ScenarioEndpoint;
import edis.krsmanovic.genapi.repository.ScenarioEndpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ScenarioEndpointProviderIT {

    @Autowired
    private ScenarioEndpointProvider scenarioEndpointProvider;

    @SpyBean
    private ScenarioEndpointRepository scenarioEndpointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEveryTest() {
        Mockito.clearInvocations(scenarioEndpointRepository);
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM EndpointResponse");
        jdbcTemplate.update("DELETE FROM ScenarioEndpoint");
    }

    @AfterEach
    public void afterEveryTest() {
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM EndpointResponse");
        jdbcTemplate.update("DELETE FROM ScenarioEndpoint");
        scenarioEndpointProvider.clear();
    }

    @Test
    public void gettingSameEndpointTwiceCallsDatabaseOnce() throws HttpEndpointNotFoundException, DatabaseException {
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));
        Integer httpId = scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>()));

        scenarioEndpointProvider.reloadAll();
        scenarioEndpointProvider.getById(httpId);
        scenarioEndpointProvider.getById(httpId);

        verify(scenarioEndpointRepository, times(0)).getById(anyInt());
        verify(scenarioEndpointRepository).getAll();
    }

    @Test
    public void gettingAllThenGettingTwiceShouldCallDatabaseOnce() throws HttpEndpointNotFoundException, DatabaseException {
        Integer httpId1 = scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));
        Integer httpId2 = scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>()));

        scenarioEndpointProvider.reloadAll();
        scenarioEndpointProvider.getById(httpId1);
        scenarioEndpointProvider.getById(httpId2);

        verify(scenarioEndpointRepository, times(0)).getById(anyInt());
        verify(scenarioEndpointRepository).getAll();
    }

    @Test
    public void reloadCachedValues() throws DatabaseException {
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>()));
        scenarioEndpointRepository.save(new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>()));

        scenarioEndpointProvider.reloadAll();

        assertThat(scenarioEndpointProvider.size()).isEqualTo(2);
    }

    @Test
    public void cacheReturnsCachedValue() throws DatabaseException, HttpEndpointNotFoundException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.GET, givenHttpHeaders(), new ArrayList<>());
        Integer scenarioEndpointId = scenarioEndpointRepository.save(scenarioEndpoint);

        scenarioEndpointProvider.reloadAll(); //caching
        //Using jdbctemplate to update a value so cache doesn't clear, and returns cached value
        jdbcTemplate.update("UPDATE ScenarioEndpoint SET HttpMethod = 'DELETE' WHERE Id=?", scenarioEndpointId);

        ScenarioEndpoint scenarioEndpoint1 = scenarioEndpointProvider.getById(scenarioEndpointId);
        assertThat(scenarioEndpoint1.getHttpMethod()).isEqualTo(HttpMethod.GET);

        scenarioEndpointProvider.reloadAll(); //update cache

        scenarioEndpoint1 = scenarioEndpointProvider.getById(scenarioEndpointId);
        assertThat(scenarioEndpoint1.getHttpMethod()).isEqualTo(HttpMethod.DELETE);
    }

    @Test
    public void reloadAllTest() throws DatabaseException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointRepository.save(scenarioEndpoint);
        scenarioEndpointRepository.save(scenarioEndpoint);
        scenarioEndpointRepository.save(scenarioEndpoint);

        long size = scenarioEndpointProvider.reloadAll();

        assertThat(size).isEqualTo(3);
    }

    @Test
    public void putTest() throws DatabaseException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointProvider.put(scenarioEndpoint);
        scenarioEndpointProvider.put(scenarioEndpoint);
        scenarioEndpointProvider.put(scenarioEndpoint);

        assertThat(scenarioEndpointProvider.size()).isEqualTo(3);
    }

    @Test
    public void removeTest() throws DatabaseException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointProvider.put(scenarioEndpoint);
        scenarioEndpointProvider.put(scenarioEndpoint);
        Integer id = scenarioEndpointProvider.put(scenarioEndpoint);

        scenarioEndpointProvider.remove(id);

        assertThat(scenarioEndpointProvider.size()).isEqualTo(2);

        long size = scenarioEndpointProvider.reloadAll();
        assertThat(size).isEqualTo(2);
    }

    @Test
    public void reloadIdTest() throws DatabaseException, HttpEndpointNotFoundException {
        ScenarioEndpoint scenarioEndpoint = new ScenarioEndpoint(null, HttpMethod.POST, givenHttpHeaders(), new ArrayList<>());
        scenarioEndpointProvider.put(scenarioEndpoint);
        scenarioEndpointProvider.put(scenarioEndpoint);
        scenarioEndpoint.setId(scenarioEndpointProvider.put(scenarioEndpoint));

        scenarioEndpoint.setHttpMethod(HttpMethod.GET);
        scenarioEndpointRepository.update(scenarioEndpoint);
        scenarioEndpointProvider.reloadId(scenarioEndpoint.getId());

        scenarioEndpoint = scenarioEndpointProvider.getById(scenarioEndpoint.getId());

        assertThat(scenarioEndpoint.getHttpMethod()).isEqualTo(HttpMethod.GET);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}