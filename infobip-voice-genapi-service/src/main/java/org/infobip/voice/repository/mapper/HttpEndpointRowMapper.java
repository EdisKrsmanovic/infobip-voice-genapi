package org.infobip.voice.repository.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.model.HttpHeader;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class HttpEndpointRowMapper implements RowMapper<HttpEndpoint> {

    private JdbcTemplate jdbcTemplate;

    @Override
    public HttpEndpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<HttpHeader> httpHeaders = jdbcTemplate.query(String.format("select * from voip.httpHeaders where HttpEndpointId = %s", rs.getInt("Id")), new BeanPropertyRowMapper<>(HttpHeader.class));
        return new HttpEndpoint(
                rs.getInt("Id"),
                HttpMethod.valueOf(rs.getString("HttpMethod")),
                httpHeaders,
                rs.getString("Body"));

    }
}