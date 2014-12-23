package org.opennms.core.config.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.config.api.FilesystemCachingConfigurationResource;

public class PathCachingConfigurationResourceTest {
    @Test
    public void testGet() throws Exception {
        Path path = Paths.get("target/test-resource/get.txt");
        Files.createDirectories(path.getParent());
        Files.write(path, "blah".getBytes(), StandardOpenOption.CREATE);

        final FilesystemCachingConfigurationResource<String> resource = new AbstractPathCachingConfigurationResource<String>(path) {
            @Override protected String load() throws ConfigurationResourceException {
                try {
                    return new String(Files.readAllBytes(getPath()));
                } catch (final IOException e) {
                    throw new ConfigurationResourceException(e);
                }
            }
            @Override protected void save(final String config) throws ConfigurationResourceException {
            }
        };

        assertEquals("get.txt", resource.getPath().getFileName().toString());
        assertEquals("blah", resource.get());
        Files.write(path, "foo".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        assertEquals("blah", resource.get());
        resource.invalidate();
        assertEquals("foo", resource.get());
    }

    @Test
    public void testPut() throws Exception {
        Path path = Paths.get("target/test-resource/put.txt");
        Files.createDirectories(path.getParent());
        Files.write(path, "blah".getBytes(), StandardOpenOption.CREATE);

        final FilesystemCachingConfigurationResource<String> resource = new AbstractPathCachingConfigurationResource<String>(path) {
            @Override protected String load() throws ConfigurationResourceException {
                try {
                    return new String(Files.readAllBytes(getPath()));
                } catch (final IOException e) {
                    throw new ConfigurationResourceException(e);
                }
            }
            @Override protected void save(final String config) throws ConfigurationResourceException {
                try {
                    Files.write(getPath(), config.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                } catch (final IOException e) {
                    throw new ConfigurationResourceException(e);
                }
            }
        };

        assertEquals("put.txt", resource.getPath().getFileName().toString());
        assertEquals("blah", resource.get());
        resource.put("foo");
        assertEquals("foo", resource.get());
        assertEquals("foo", new String(Files.readAllBytes(path)));
    }
}
