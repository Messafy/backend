package com.luispiquinrey.backend.share.infrastructure;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ProfileConfigurationTest {
    @Test
    @Timeout(1)
    @Tag("profileConfiguration")
    void productionRequiresSecretsAndPublicUrlsWithoutImportingLocalFiles() throws IOException {
        var env = profile("prod");
        assertNull(env.getProperty("spring.config.import"));
        assertThrows(IllegalArgumentException.class, () -> env.getProperty("security.jwt.secret"));
        assertThrows(IllegalArgumentException.class, () -> env.getProperty("app.frontend-url"));
        assertThrows(IllegalArgumentException.class, () -> env.getProperty("app.public-base-url"));
        env.setProperty("JWT_SECRET", "provided-secret");
        env.setProperty("APP_FRONTEND_URL", "https://notes.example.com");
        env.setProperty("APP_PUBLIC_BASE_URL", "https://api.example.com");
        assertEquals("provided-secret", env.getProperty("security.jwt.secret"));
        assertEquals("https://notes.example.com", env.getProperty("app.frontend-url"));
        assertEquals("https://api.example.com", env.getProperty("app.public-base-url"));
    }

    @Test
    @Timeout(1)
    @Tag("profileConfiguration")
    void testProfileNeedsNeitherLocalSecretsNorSmtp() throws IOException {
        var env = profile("test");
        assertNull(env.getProperty("spring.config.import"));
        assertNull(env.getProperty("spring.mail.host"));
        assertNull(env.getProperty("spring.mail.username"));
        assertNull(env.getProperty("spring.mail.password"));
        assertTrue(env.getProperty("security.jwt.secret").startsWith("test-only-"));
        assertEquals("luispiquinrey", env.getProperty("security.user.username"));
        assertEquals("{noop}test", env.getProperty("security.user.password"));
        assertEquals("false", env.getProperty("management.opentelemetry.enabled"));
        assertEquals("false", env.getProperty("management.otlp.metrics.export.enabled"));
    }

    @Test
    @Timeout(1)
    @Tag("profileConfiguration")
    void noProfileInheritsRequestOrDatabaseDebugLogging() throws IOException {
        for (String profile : new String[]{"dev", "test", "prod"}) {
            var env = profile(profile);
            assertEquals("WARN", env.getProperty("logging.level.org.springframework.web"));
            assertEquals("WARN", env.getProperty("logging.level.org.springframework.data.mongodb.core.MongoTemplate"));
            assertEquals("false", env.getProperty("spring.mvc.log-request-details"));
            assertEquals(profile.equals("dev") ? "DEBUG" : "INFO",
                    env.getProperty("logging.level.com.luispiquinrey.backend"));
        }
    }

    @Test
    @Timeout(1)
    @Tag("profileConfiguration")
    void smtpUsesOneTlsModeWithBoundedTimeouts() throws IOException {
        for (String profile : new String[]{"dev", "prod"}) {
            var env = profile(profile);
            assertEquals("465", env.getProperty("spring.mail.port"));
            assertEquals("true", env.getProperty("spring.mail.properties.mail.smtp.ssl.enable"));
            assertEquals("false", env.getProperty("spring.mail.properties.mail.smtp.starttls.enable"));
            for (String timeout : new String[]{"connectiontimeout", "timeout", "writetimeout"}) {
                assertEquals("5000", env.getProperty("spring.mail.properties.mail.smtp." + timeout));
            }
        }
    }

    @Test
    @Timeout(1)
    @Tag("profileConfiguration")
    void devImportsExternalEnvButSecretsAreNotOnClasspath() throws IOException {
        assertEquals("optional:file:./.env[.properties]", profile("dev").getProperty("spring.config.import"));
        assertFalse(new ClassPathResource(".env").exists());
    }

    @Test
    @Timeout(1)
    @Tag("profileConfiguration")
    void telemetryUsesMessafyServiceIdentity() throws IOException {
        for (String profile : new String[]{"dev", "prod"}) {
            var env = profile(profile);
            assertEquals("messafy-backend", env.getProperty("management.opentelemetry.resource-attributes.service.name"));
            assertEquals("messafy", env.getProperty("management.opentelemetry.resource-attributes.service.namespace"));
        }
    }

    private MockEnvironment profile(String profile) throws IOException {
        var env = new MockEnvironment();
        var loader = new YamlPropertySourceLoader();
        for (String resource : new String[]{"application.yaml", "application-" + profile + ".yml"}) {
            loader.load(resource, new ClassPathResource(resource))
                    .forEach(source -> env.getPropertySources().addFirst(source));
        }
        return env;
    }
}
