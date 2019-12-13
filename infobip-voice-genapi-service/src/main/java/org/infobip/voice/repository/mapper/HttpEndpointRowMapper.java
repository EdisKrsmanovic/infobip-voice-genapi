package org.infobip.voice.repository.mapper;

import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.model.HttpHeader;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class HttpEndpointRowMapper implements RowMapper<HttpEndpoint> {

    @Override
    public HttpEndpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
        HttpEndpoint httpEndpoint = new HttpEndpoint(
                rs.getInt("Id"),
                HttpMethod.valueOf(rs.getString("HttpMethod")),
                Collections.emptyList(),
                rs.getString("Body"));

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