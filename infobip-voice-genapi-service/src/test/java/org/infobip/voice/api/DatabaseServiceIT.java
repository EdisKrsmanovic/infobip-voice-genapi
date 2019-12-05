package org.infobip.voice.api;

import org.infobip.voice.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class DatabaseServiceIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void notEmptyDatabase() throws SQLException {
        Integer numberOfTables = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES", Integer.class);
        assertThat(numberOfTables).isNotEqualTo(0);
    }
}
