package org.infobip.voice.genapi.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.connector.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.repository.SingleResponseEndpointRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Data
@Slf4j
@Component
public class SingleResponseEndpointProvider implements EndpointProvider<SingleResponseEndpoint> {

    @Autowired
    private SingleResponseEndpointRepository singleResponseEndpointRepository;

    private LoadingCache<Integer, SingleResponseEndpoint> cachedEndpoints = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public SingleResponseEndpoint load(@NotNull Integer singleResponseEndpointId) {
                    return null;
                }
            });

    public long reloadAll() {
        log.info("Reloading cached Single Response Endpoint values");
        List<SingleResponseEndpoint> singleResponseEndpoints = singleResponseEndpointRepository.getAll();
        cachedEndpoints.invalidateAll();
        singleResponseEndpoints.forEach(e -> cachedEndpoints.put(e.getId(), e));
        return singleResponseEndpoints.size();
    }

    public SingleResponseEndpoint reloadId(Integer singleResponseEndpointId) {
        log.info("Reloading cached http endpoint value with id {}", singleResponseEndpointId);
        SingleResponseEndpoint singleResponseEndpoint = singleResponseEndpointRepository.getById(singleResponseEndpointId);
        cachedEndpoints.invalidate(singleResponseEndpointId);
        cachedEndpoints.put(singleResponseEndpointId, singleResponseEndpoint);
        return singleResponseEndpoint;
    }

    public SingleResponseEndpoint getById(Integer singleResponseEndpointId) throws HttpEndpointNotFoundException {
        try {
            return cachedEndpoints.get(singleResponseEndpointId);
        } catch (CacheLoader.InvalidCacheLoadException | ExecutionException e) {
            log.warn(e.getMessage());
            throw new HttpEndpointNotFoundException(singleResponseEndpointId, e);
        }
    }

    public int put(SingleResponseEndpoint singleResponseEndpoint) throws DatabaseException {
        Integer singleResponseEndpointId = singleResponseEndpointRepository.save(singleResponseEndpoint);
        singleResponseEndpoint.setId(singleResponseEndpointId);
        cachedEndpoints.put(singleResponseEndpointId, singleResponseEndpoint);
        return singleResponseEndpointId;
    }

    public void remove(Integer singleResponseEndpointId) throws DatabaseException {
        singleResponseEndpointRepository.remove(singleResponseEndpointId);
        cachedEndpoints.invalidate(singleResponseEndpointId);
    }

    public void clear() {
        cachedEndpoints.invalidateAll();
    }

    public void update(SingleResponseEndpoint singleResponseEndpoint) throws DatabaseException {
        singleResponseEndpointRepository.update(singleResponseEndpoint);
    }

    public long size() {
        return cachedEndpoints.size();
    }

    @Scheduled(fixedDelay = 60000) //every minute
    private void startCaching() {
        this.reloadAll();
    }

    private Collection<SingleResponseEndpoint> getAll() {
        return cachedEndpoints.asMap().values();
    }
}
