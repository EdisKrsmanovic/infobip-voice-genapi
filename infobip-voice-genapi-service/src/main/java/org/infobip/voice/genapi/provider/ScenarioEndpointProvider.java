package org.infobip.voice.genapi.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.connector.model.EndpointResponse;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.connector.model.ScenarioEndpoint;
import org.infobip.voice.genapi.repository.ScenarioEndpointRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@Data
@Slf4j
@Component
public class ScenarioEndpointProvider implements EndpointProvider<ScenarioEndpoint> {

    @Autowired
    private ScenarioEndpointRepository scenarioEndpointRepository;

    private LoadingCache<Integer, ScenarioEndpoint> cachedScenarioEndpoints = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public ScenarioEndpoint load(@NotNull Integer scenarioEndpointId) {
                    return null;
                }
            });

    public long reloadAll() {
        log.info("Reloading cached Scenario Endpoint values");
        List<ScenarioEndpoint> scenarioEndpoints = scenarioEndpointRepository.getAll();
        ConcurrentMap<Integer, ScenarioEndpoint> oldScenarioEndpointsMap = cachedScenarioEndpoints.asMap();
        cachedScenarioEndpoints.invalidateAll();
        scenarioEndpoints.forEach(e -> cacheAndCheckNextResponseNumber(oldScenarioEndpointsMap, e));
        return scenarioEndpoints.size();
    }

    public ScenarioEndpoint reloadId(Integer scenarioEndpointId) {
        log.info("Reloading cached Scenario Endpoint value with id {}", scenarioEndpointId);
        ScenarioEndpoint scenarioEndpoint = scenarioEndpointRepository.getById(scenarioEndpointId);
        cachedScenarioEndpoints.invalidate(scenarioEndpointId);
        cachedScenarioEndpoints.put(scenarioEndpointId, scenarioEndpoint);
        return scenarioEndpoint;
    }

    public ScenarioEndpoint getById(Integer scenarioEndpointId) throws HttpEndpointNotFoundException {
        try {
            return cachedScenarioEndpoints.get(scenarioEndpointId);
        } catch (CacheLoader.InvalidCacheLoadException | ExecutionException e) {
            log.warn(e.getMessage());
            throw new HttpEndpointNotFoundException(scenarioEndpointId, e);
        }
    }

    public int put(ScenarioEndpoint scenarioEndpoint) throws DatabaseException {
        Integer scenarioEndpointId = scenarioEndpointRepository.save(scenarioEndpoint);
        cachedScenarioEndpoints.put(scenarioEndpointId, scenarioEndpoint);
        return scenarioEndpointId;
    }

    public void put(Integer scenarioEndpointId, EndpointResponse endpointResponse) throws HttpEndpointNotFoundException {
        if(cachedScenarioEndpoints.getIfPresent(scenarioEndpointId) == null) {
            throw new HttpEndpointNotFoundException(scenarioEndpointId, new Exception("Scenario Endpoint not found while trying to add response"));
        }

        try {
            ScenarioEndpoint ScenarioEndpoint = cachedScenarioEndpoints.get(scenarioEndpointId);

            List<EndpointResponse> endpointResponses = ScenarioEndpoint.getEndpointResponses();

            endpointResponse.setOrdinalNumber(endpointResponses.size() + 1);

            scenarioEndpointRepository.save(ScenarioEndpoint.getId(), endpointResponse);
            endpointResponses.add(endpointResponse);
        } catch (ExecutionException e) {
            log.error("Error while trying to save Scenario Endpoint response, message: {}", e.getMessage());
        }
    }

    public void remove(Integer scenarioEndpointId) throws DatabaseException {
        scenarioEndpointRepository.remove(scenarioEndpointId);
        cachedScenarioEndpoints.invalidate(scenarioEndpointId);
    }

    public void clear() {
        cachedScenarioEndpoints.invalidateAll();
    }

    public void update(ScenarioEndpoint scenarioEndpoint) throws DatabaseException {
        scenarioEndpointRepository.update(scenarioEndpoint);
    }

    public long size() {
        return cachedScenarioEndpoints.size();
    }

    private void cacheAndCheckNextResponseNumber(ConcurrentMap<Integer, ScenarioEndpoint> oldScenarioEndpointsMap, ScenarioEndpoint newScenarioEndpoint) {
        cachedScenarioEndpoints.put(newScenarioEndpoint.getId(), newScenarioEndpoint);
        ScenarioEndpoint oldScenarioEndpoint = oldScenarioEndpointsMap.get(newScenarioEndpoint.getId());
        if (oldScenarioEndpoint != null) {
            if (ChronoUnit.MINUTES.between(oldScenarioEndpoint.getResponseFirstAccessTime(), LocalTime.now()) > 5) {
                newScenarioEndpoint.getNextResponseNo().set(0);
                newScenarioEndpoint.setResponseFirstAccessTime(LocalTime.now());
            } else {
                newScenarioEndpoint.setNextResponseNo(oldScenarioEndpoint.getNextResponseNo());
                newScenarioEndpoint.setResponseFirstAccessTime(oldScenarioEndpoint.getResponseFirstAccessTime());
            }
        }
    }

    @Scheduled(fixedDelay = 60000) //every minute
    private void startCaching() {
        this.reloadAll();
    }

    private Collection<ScenarioEndpoint> getAll() {
        return cachedScenarioEndpoints.asMap().values();
    }
}