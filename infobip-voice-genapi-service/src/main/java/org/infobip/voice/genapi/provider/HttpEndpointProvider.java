package org.infobip.voice.genapi.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.model.HttpEndpoint;
import org.infobip.voice.genapi.repository.HttpEndpointRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Data
@Slf4j
@Component
@EnableScheduling
public class HttpEndpointProvider {

    @Autowired
    private HttpEndpointRepository httpEndpointRepository;

    private LoadingCache<Integer, HttpEndpoint> cachedHttpEndpoints = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public HttpEndpoint load(@NotNull Integer httpEndpointId) {
                    return null;
                }
            });

    public int reloadAll() {
        log.info("Reloading cached values");
        List<HttpEndpoint> httpEndpoints = httpEndpointRepository.getAll();
        cachedHttpEndpoints.invalidateAll();
        httpEndpoints.forEach(e -> cachedHttpEndpoints.put(e.getId(), e));
        return httpEndpoints.size();
    }

    public HttpEndpoint reloadId(Integer httpEndpointId) {
        log.info(String.format("Reloading cached http endpoint value with id %s", httpEndpointId));
        HttpEndpoint httpEndpoint = httpEndpointRepository.getById(httpEndpointId);
        cachedHttpEndpoints.invalidate(httpEndpointId);
        cachedHttpEndpoints.put(httpEndpointId, httpEndpoint);
        return httpEndpoint;
    }

    public HttpEndpoint getById(Integer httpEndpointId) throws HttpEndpointNotFoundException {
        try {
            return cachedHttpEndpoints.get(httpEndpointId);
        } catch (CacheLoader.InvalidCacheLoadException | ExecutionException e) {
            log.warn(e.getMessage());
            throw new HttpEndpointNotFoundException(httpEndpointId, e);
        }
    }

    public int put(HttpEndpoint httpEndpoint) throws DatabaseException {
        Integer httpEndpointId = httpEndpointRepository.save(httpEndpoint);
        httpEndpoint.setId(httpEndpointId);
        cachedHttpEndpoints.put(httpEndpointId, httpEndpoint);
        return httpEndpointId;
    }

    public void remove(Integer httpEndpointId) throws DatabaseException {
        httpEndpointRepository.remove(httpEndpointId);
        cachedHttpEndpoints.invalidate(httpEndpointId);
    }

    public void clear() {
        cachedHttpEndpoints.invalidateAll();
    }

    public long size() {
        return cachedHttpEndpoints.size();
    }

    @Scheduled(fixedDelay = 60000) //every minute
    private void startCaching() {
        this.reloadAll();
    }

    private Collection<HttpEndpoint> getAll() {
        return cachedHttpEndpoints.asMap().values();
    }
}
