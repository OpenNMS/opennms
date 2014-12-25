package org.opennms.core.config.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opennms.core.config.api.FilesystemCachingConfigurationResource;
import org.opennms.core.test.MockLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class PathCachingConfigurationResourceWatcherTest {
    private static final Logger LOG = LoggerFactory.getLogger(PathCachingConfigurationResourceWatcherTest.class);

    private PathCachingConfigurationResourceWatcher m_watcher = null;
    private FilesystemCachingConfigurationResource<String> m_resource = null;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");
        m_watcher = new PathCachingConfigurationResourceWatcher();
        m_resource = mock(FilesystemCachingConfigurationResource.class);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_resource);
        m_watcher.shutdown();
    }

    @Test
    public void testInitialization() throws Exception {
        final Path workDirectory = Files.createTempDirectory("test");
        when(m_resource.getPath()).thenReturn(workDirectory);
        m_watcher.afterPropertiesSet();
        m_watcher.addResource(m_resource);
        verify(m_resource, times(1)).getPath();
    }

    @Test
    public void testChange() throws Exception {
        final Path workDirectory = Files.createTempDirectory("test");
        when(m_resource.getPath()).thenReturn(workDirectory);
        m_watcher.afterPropertiesSet();
        m_watcher.addResource(m_resource);
        verify(m_resource, times(1)).getPath();
        final Path outputPath = Paths.get(workDirectory.toString(), "foo.txt");
        Thread.sleep(10000);
        LOG.debug("Writing to {}", outputPath);
        Files.write(outputPath, "blah".getBytes(), StandardOpenOption.CREATE_NEW);
        verify(m_resource, timeout(20000).times(1)).invalidate();
        Files.delete(outputPath);
        verify(m_resource, timeout(20000).times(2)).invalidate();
    }
}
