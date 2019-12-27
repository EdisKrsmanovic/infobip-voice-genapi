package org.infobip.voice.genapi.repository.mapper;

import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.model.HttpHeader;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SingleResponseEndpointRowMapper implements RowMapper<SingleResponseEndpoint> {

    @Override
    public SingleResponseEndpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
        SingleResponseEndpoint singleResponseEndpoint = new SingleResponseEndpoint(
                rs.getInt("Id"),
                HttpMethod.valueOf(rs.getString("HttpMethod")),
                Collections.emptyList(),
                rs.getString("Response"));

        List<HttpHeader> httpHeaders = new ArrayList<>();
        do {
            addHttpHeaderToList(rs, httpHeaders);
        } while (rs.next());

        singleResponseEndpoint.setHttpHeaders(httpHeaders);

        return singleResponseEndpoint;

    }

    private void addHttpHeaderToList(ResultSet rs, List<HttpHeader> httpHeaders) throws SQLException {
        String name = rs.getString("Name");
        String value = rs.getString("Value");
        if (name != null && value != null) {
            httpHeaders.add(new HttpHeader(name, value));
        }
    }
}