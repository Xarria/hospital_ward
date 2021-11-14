package com.backend.hospitalward.integration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestContainer.DockerMysqlDataSourceInitializer.class)
@Testcontainers
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractTestContainer {

    @LocalServerPort
    int port;

    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.26");

    static {
        mySQLContainer.start();
    }

    public String getUrlWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    public static class DockerMysqlDataSourceInitializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.password=" + mySQLContainer.getPassword()
            );
        }
    }
}
