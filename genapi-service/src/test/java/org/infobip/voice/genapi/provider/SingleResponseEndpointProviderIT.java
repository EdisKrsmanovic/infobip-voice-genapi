package edis.krsmanovic.genapi.provider;

import edis.krsmanovic.genapi.Application;
import edis.krsmanovic.genapi.exception.DatabaseException;
import edis.krsmanovic.genapi.exception.HttpEndpointNotFoundException;
import edis.krsmanovic.genapi.model.HttpHeader;
import edis.krsmanovic.genapi.model.HttpMethod;
import edis.krsmanovic.genapi.model.SingleResponseEndpoint;
import edis.krsmanovic.genapi.repository.SingleResponseEndpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SingleResponseEndpointProviderIT {

    @Autowired
    private SingleResponseEndpointProvider singleResponseEndpointProvider;

    @SpyBean
    private SingleResponseEndpointRepository singleResponseEndpointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEveryTest() {
        Mockito.clearInvocations(singleResponseEndpointRepository);
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM SingleResponseEndpoint");
    }

    @AfterEach
    public void afterEveryTest() {
        jdbcTemplate.update("DELETE FROM EndpointHeader");
        jdbcTemplate.update("DELETE FROM SingleResponseEndpoint");
        singleResponseEndpointProvider.clear();
    }

    @Test
    public void gettingSameEndpointTwiceCallsDatabaseOnce() throws HttpEndpointNotFoundException, DatabaseException {
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        Integer httpId = singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));

        singleResponseEndpointProvider.reloadAll();
        singleResponseEndpointProvider.getById(httpId);
        singleResponseEndpointProvider.getById(httpId);

        verify(singleResponseEndpointRepository, times(0)).getById(anyInt());
        verify(singleResponseEndpointRepository).getAll();
    }

    @Test
    public void gettingAllThenGettingTwiceShouldCallDatabaseOnce() throws HttpEndpointNotFoundException, DatabaseException {
        Integer httpId1 = singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        Integer httpId2 = singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));

        singleResponseEndpointProvider.reloadAll();
        singleResponseEndpointProvider.getById(httpId1);
        singleResponseEndpointProvider.getById(httpId2);

        verify(singleResponseEndpointRepository, times(0)).getById(anyInt());
        verify(singleResponseEndpointRepository).getAll();
    }

    @Test
    public void reloadCachedValues() throws DatabaseException {
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        singleResponseEndpointRepository.save(new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));

        singleResponseEndpointProvider.reloadAll();

//        verify(singleResponseEndpointRepository, times(2)).getAll();
        assertThat(singleResponseEndpointProvider.size()).isEqualTo(2);
    }

    @Test
    public void cacheReturnsCachedValue() throws DatabaseException, HttpEndpointNotFoundException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}");
        Integer singlResponseEndpointId = singleResponseEndpointRepository.save(singleResponseEndpoint);

        singleResponseEndpointProvider.reloadAll(); //caching
        //Using jdbctemplate to update a value so cache doesn't clear, and returns cached value
        jdbcTemplate.update("UPDATE SingleResponseEndpoint SET HttpMethod = 'DELETE' WHERE Id=?", singlResponseEndpointId);

        SingleResponseEndpoint singleResponseEndpoint1 = singleResponseEndpointProvider.getById(singlResponseEndpointId);
        assertThat(singleResponseEndpoint1.getHttpMethod()).isEqualTo(HttpMethod.GET);

        singleResponseEndpointProvider.reloadAll(); //update cache

        singleResponseEndpoint1 = singleResponseEndpointProvider.getById(singlResponseEndpointId);
        assertThat(singleResponseEndpoint1.getHttpMethod()).isEqualTo(HttpMethod.DELETE);
    }

    @Test
    public void reloadAllTest() throws DatabaseException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        singleResponseEndpointRepository.save(singleResponseEndpoint);
        singleResponseEndpointRepository.save(singleResponseEndpoint);
        singleResponseEndpointRepository.save(singleResponseEndpoint);

        long size = singleResponseEndpointProvider.reloadAll();

        assertThat(size).isEqualTo(3);
    }

    @Test
    public void putTest() throws DatabaseException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        singleResponseEndpointProvider.put(singleResponseEndpoint);
        singleResponseEndpointProvider.put(singleResponseEndpoint);
        singleResponseEndpointProvider.put(singleResponseEndpoint);

        assertThat(singleResponseEndpointProvider.size()).isEqualTo(3);
    }

    @Test
    public void removeTest() throws DatabaseException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        singleResponseEndpointProvider.put(singleResponseEndpoint);
        singleResponseEndpointProvider.put(singleResponseEndpoint);
        Integer id = singleResponseEndpointProvider.put(singleResponseEndpoint);

        singleResponseEndpointProvider.remove(id);

        assertThat(singleResponseEndpointProvider.size()).isEqualTo(2);

        long size = singleResponseEndpointProvider.reloadAll();
        assertThat(size).isEqualTo(2);
    }

    @Test
    public void reloadIdTest() throws DatabaseException, HttpEndpointNotFoundException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        singleResponseEndpointProvider.put(singleResponseEndpoint);
        singleResponseEndpointProvider.put(singleResponseEndpoint);
        singleResponseEndpoint.setId(singleResponseEndpointProvider.put(singleResponseEndpoint));

        singleResponseEndpoint.setHttpMethod(HttpMethod.GET);
        singleResponseEndpointRepository.update(singleResponseEndpoint);
        singleResponseEndpointProvider.reloadId(singleResponseEndpoint.getId());

        singleResponseEndpoint = singleResponseEndpointProvider.getById(singleResponseEndpoint.getId());

        assertThat(singleResponseEndpoint.getHttpMethod()).isEqualTo(HttpMethod.GET);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}
