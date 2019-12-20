package org.infobip.voice.genapi.provider;

import org.infobip.voice.genapi.Application;
import org.infobip.voice.genapi.TestConfiguration;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.provider.HttpEndpointProvider;
import org.infobip.voice.genapi.model.HttpEndpoint;
import org.infobip.voice.genapi.model.HttpHeader;
import org.infobip.voice.genapi.repository.HttpEndpointRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class HttpEndpointProviderIT {

    @Autowired
    private HttpEndpointProvider httpEndpointProvider;

    @SpyBean
    private HttpEndpointRepository httpEndpointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void beforeEveryTest() {
        Mockito.clearInvocations(httpEndpointRepository);
        jdbcTemplate.update("DELETE FROM voip.HttpHeaders");
        jdbcTemplate.update("DELETE FROM voip.HttpEndpoint");
    }

    @After
    public void afterEveryTest() {
        jdbcTemplate.update("DELETE FROM voip.HttpHeaders");
        jdbcTemplate.update("DELETE FROM voip.HttpEndpoint");
        httpEndpointProvider.clear();
    }

    @Test
    public void gettingSameEndpointTwiceCallsDatabaseOnce() throws HttpEndpointNotFoundException, DatabaseException {
        httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        Integer httpId = httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));

        httpEndpointProvider.reloadAll();
        httpEndpointProvider.getById(httpId);
        httpEndpointProvider.getById(httpId);

        verify(httpEndpointRepository, times(0)).getById(anyInt());
        verify(httpEndpointRepository).getAll();
    }

    @Test
    public void gettingAllThenGettingTwiceShouldCallDatabaseOnce() throws HttpEndpointNotFoundException, DatabaseException {
        Integer httpId1 = httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        Integer httpId2 = httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));

        httpEndpointProvider.reloadAll();
        httpEndpointProvider.getById(httpId1);
        httpEndpointProvider.getById(httpId2);

        verify(httpEndpointRepository, times(0)).getById(anyInt());
        verify(httpEndpointRepository).getAll();
    }

    @Test
    public void reloadCachesValues() throws DatabaseException {
        httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}"));
        httpEndpointRepository.save(new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{body}"));

        httpEndpointProvider.reloadAll();

        verify(httpEndpointRepository, times(1)).getAll();
        assertThat(httpEndpointProvider.size()).isEqualTo(2);
    }

    @Test
    public void cacheReturnsCachedValue() throws DatabaseException, HttpEndpointNotFoundException {
        HttpEndpoint httpEndpoint = new HttpEndpoint(null, HttpMethod.GET, givenHttpHeaders(), "{}");
        Integer httpEndpointId = httpEndpointRepository.save(httpEndpoint);

        httpEndpointProvider.reloadAll(); //caching
        //Using jdbctemplate to update a value so cache doesn't clear, and returns cached value
        jdbcTemplate.update("UPDATE voip.HttpEndpoint SET HttpMethod = 'DELETE' WHERE Id=?", httpEndpointId);

        HttpEndpoint httpEndpoint1 = httpEndpointProvider.getById(httpEndpointId);
        assertThat(httpEndpoint1.getHttpMethod()).isEqualTo(HttpMethod.GET);

        httpEndpointProvider.reloadAll(); //update cache

        httpEndpoint1 = httpEndpointProvider.getById(httpEndpointId);
        assertThat(httpEndpoint1.getHttpMethod()).isEqualTo(HttpMethod.DELETE);
    }

    @Test
    public void reloadAllTest() throws DatabaseException {
        HttpEndpoint httpEndpoint = new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        httpEndpointRepository.save(httpEndpoint);
        httpEndpointRepository.save(httpEndpoint);
        httpEndpointRepository.save(httpEndpoint);

        long size = httpEndpointProvider.reloadAll();

        assertThat(size).isEqualTo(3);
    }

    @Test
    public void putTest() throws DatabaseException {
        HttpEndpoint httpEndpoint = new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        httpEndpointProvider.put(httpEndpoint);
        httpEndpointProvider.put(httpEndpoint);
        httpEndpointProvider.put(httpEndpoint);

        assertThat(httpEndpointProvider.size()).isEqualTo(3);
    }

    @Test
    public void removeTest() throws DatabaseException {
        HttpEndpoint httpEndpoint = new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        httpEndpointProvider.put(httpEndpoint);
        httpEndpointProvider.put(httpEndpoint);
        Integer id = httpEndpointProvider.put(httpEndpoint);

        httpEndpointProvider.remove(id);

        assertThat(httpEndpointProvider.size()).isEqualTo(2);

        long size = httpEndpointProvider.reloadAll();
        assertThat(size).isEqualTo(2);
    }

    @Test
    public void reloadIdTest() throws DatabaseException, HttpEndpointNotFoundException {
        HttpEndpoint httpEndpoint = new HttpEndpoint(null, HttpMethod.POST, givenHttpHeaders(), "{}");
        httpEndpointProvider.put(httpEndpoint);
        httpEndpointProvider.put(httpEndpoint);
        httpEndpoint.setId(httpEndpointProvider.put(httpEndpoint));

        httpEndpoint.setHttpMethod(HttpMethod.GET);
        httpEndpointRepository.update(httpEndpoint);
        httpEndpointProvider.reloadId(httpEndpoint.getId());

        httpEndpoint = httpEndpointProvider.getById(httpEndpoint.getId());

        assertThat(httpEndpoint.getHttpMethod()).isEqualTo(HttpMethod.GET);
    }

    private List<HttpHeader> givenHttpHeaders() {
        return List.of(new HttpHeader("Accept", "text/html"),
                new HttpHeader("Keep-Alive", "300"),
                new HttpHeader("Connection", "keep-alive"));
    }
}
