package org.testcontainers.utility;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertFalse;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

public class TestcontainersConfigurationTest {

    private Properties userProperties;
    private Properties classpathProperties;
    private Map<String, String> environment;

    @Before
    public void setUp() {
        userProperties = new Properties();
        classpathProperties = new Properties();
        environment = new HashMap<>();
    }

    @Test
    public void shouldSubstituteImageNamesFromClasspathProperties() {
        classpathProperties.setProperty("ryuk.container.image", "foo:version");
        assertEquals(
            "an image name can be pulled from classpath properties",
            DockerImageName.parse("foo:version"),
            newConfig().getConfiguredSubstituteImage(DockerImageName.parse("testcontainers/ryuk:any"))
        );
    }

    @Test
    public void shouldSubstituteImageNamesFromUserProperties() {
        userProperties.setProperty("ryuk.container.image", "foo:version");
        assertEquals(
            "an image name can be pulled from user properties",
            DockerImageName.parse("foo:version"),
            newConfig().getConfiguredSubstituteImage(DockerImageName.parse("testcontainers/ryuk:any"))
        );
    }

    @Test
    public void shouldSubstituteImageNamesFromEnvironmentVariables() {
        environment.put("TESTCONTAINERS_RYUK_CONTAINER_IMAGE", "foo:version");
        assertEquals(
            "an image name can be pulled from an environment variable",
            DockerImageName.parse("foo:version"),
            newConfig().getConfiguredSubstituteImage(DockerImageName.parse("testcontainers/ryuk:any"))
        );
    }

    @Test
    public void shouldReadChecksFromUserPropertiesOrEnvironmentOnly() {
        assertFalse("checks enabled by default", newConfig().isDisableChecks());

        classpathProperties.setProperty("checks.disable", "true");
        assertFalse("checks are not affected by classpath properties", newConfig().isDisableChecks());

        userProperties.setProperty("checks.disable", "true");
        assertTrue("checks disabled via user properties", newConfig().isDisableChecks());

        userProperties.remove("checks.disable");
        environment.put("TESTCONTAINERS_CHECKS_DISABLE", "true");
        assertTrue("checks disabled via env var", newConfig().isDisableChecks());
    }

    @Test
    public void shouldReadDockerClientStrategyFromUserPropertiesOrEnvironmentOnly() {
        String currentValue = newConfig().getDockerClientStrategyClassName();

        classpathProperties.setProperty("docker.client.strategy", UUID.randomUUID().toString());
        assertEquals("Docker client strategy is not affected by classpath properties", currentValue, newConfig().getDockerClientStrategyClassName());

        userProperties.setProperty("docker.client.strategy", "foo");
        assertEquals("Docker client strategy is changed by user property", "foo", newConfig().getDockerClientStrategyClassName());

        userProperties.remove("docker.client.strategy");
        environment.put("TESTCONTAINERS_DOCKER_CLIENT_STRATEGY", "foo");
        assertEquals("Docker client strategy is changed by env var", "foo", newConfig().getDockerClientStrategyClassName());
    }

    @Test
    public void shouldReadReuseFromUserPropertiesOrEnvironmentOnly() {
        assertFalse("no reuse by default", newConfig().environmentSupportsReuse());

        classpathProperties.setProperty("testcontainers.reuse.enable", "true");
        assertFalse("reuse is not affected by classpath properties", newConfig().environmentSupportsReuse());

        userProperties.setProperty("testcontainers.reuse.enable", "true");
        assertTrue("reuse enabled via user property", newConfig().environmentSupportsReuse());

        userProperties.remove("testcontainers.reuse.enable");
        environment.put("TESTCONTAINERS_REUSE_ENABLE", "true");
        assertTrue("reuse enabled via env var", newConfig().environmentSupportsReuse());
    }

    @Test
    public void shouldTrimImageNames() {
        userProperties.setProperty("ryuk.container.image", " testcontainersofficial/ryuk:0.3.0 ");
        assertEquals("trailing whitespace was not removed from image name property", "testcontainersofficial/ryuk:0.3.0",newConfig().getRyukImage());
    }

    private TestcontainersConfiguration newConfig() {
        return new TestcontainersConfiguration(userProperties, classpathProperties, environment);
    }
}
