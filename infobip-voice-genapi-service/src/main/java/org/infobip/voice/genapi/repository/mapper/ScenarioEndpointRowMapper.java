package org.infobip.voice.genapi.repository.mapper;

import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.model.HttpHeader;
import org.infobip.voice.genapi.model.HttpMethod;
import org.infobip.voice.genapi.model.ScenarioEndpoint;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ScenarioEndpointRowMapper implements RowMapper<ScenarioEndpoint> {

    @Override
    public ScenarioEndpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScenarioEndpoint httpEndpoint = new ScenarioEndpoint(
                rs.getInt("Id"),
                HttpMethod.valueOf(rs.getString("HttpMethod")),
                Collections.emptyList(),
                Collections.emptyList());

        List<HttpHeader> httpHeaders = new ArrayList<>();
        do {
            addHttpHeaderToList(rs, httpHeaders);
        } while (rs.next());

        httpEndpoint.setHttpHeaders(httpHeaders);

        return httpEndpoint;

    }

    private void addHttpHeaderToList(ResultSet rs, List<HttpHeader> httpHeaders) throws SQLException {
        String name = rs.getString("Name");
        String value = rs.getString("Value");
        if (name != null && value != null) {
            httpHeaders.add(new HttpHeader(name, value));
        }
    }
}