package org.opennms.core.config.impl;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opennms.core.config.api.FilesystemCachingConfigurationResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Sets;

public class PathCachingConfigurationResourceWatcher implements InitializingBean {
    public static class ResourceWatcher {
        private final WatchService m_watchService;
        private final Path m_path;
        private final Set<FilesystemCachingConfigurationResource<?>> m_resources = Sets.newConcurrentHashSet();
        private WatchKey m_watchKey = null;

        public ResourceWatcher(final WatchService watchService, final Path path) {
            m_watchService = watchService;
            m_path = path;
        }

        public void register(final FilesystemCachingConfigurationResource<?> resource) throws IOException {
            m_resources.add(resource);
            if (m_watchKey == null) {
                m_watchKey = m_path.register(m_watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                LOG.debug("Watching directory {} for changes with watch key {}.", m_path, m_watchKey);
            }
        }

        public void unregister(final FilesystemCachingConfigurationResource<?> resource) {
            m_resources.remove(resource);
            if (m_resources.size() == 0) {
                LOG.debug("All resources are unregistered for path {}, canceling directory watch.", m_path);
                m_watchKey.cancel();
                m_watchKey = null;
            }
        }

        public void invalidate() {
            LOG.debug("Directory {} has changed.  Invalidating configurations.", m_path);
            for (final FilesystemCachingConfigurationResource<?> resource : m_resources) {
                resource.invalidate();
            }
        }

        public void cancel() {
            if (m_watchKey != null || m_resources.size() > 0) {
                LOG.debug("Clearing watch key {} for path {}", m_watchKey, m_path);
                m_watchKey.cancel();
            }
            m_resources.clear();
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(PathCachingConfigurationResourceWatcher.class);

    private final Map<Path, ResourceWatcher> m_watchers = new ConcurrentHashMap<>();
    private final ExecutorService m_executor = Executors.newSingleThreadExecutor();
    private final WatchService m_watchService;
    private Future<?> m_watchServiceMonitor = null;

    public PathCachingConfigurationResourceWatcher(final FilesystemCachingConfigurationResource<?>... resources) throws IOException {
        m_watchService = FileSystems.getDefault().newWatchService();

        for (final FilesystemCachingConfigurationResource<?> resource : resources) {
            addResource(resource);
        }
    }

    public void addResource(final FilesystemCachingConfigurationResource<?> resource) throws IOException {
        final ResourceWatcher watcher = getWatcher(resource);
        watcher.register(resource);
    }

    public void removeResource(final FilesystemCachingConfigurationResource<?> resource) throws IOException {
        if (resource == null) {
            return;
        }
        final ResourceWatcher watcher = getWatcher(resource);
        watcher.unregister(resource);
    }

    public void shutdown() {
        if (m_watchServiceMonitor != null) {
            m_watchServiceMonitor.cancel(true);
        }
    }

    protected ResourceWatcher getWatcher(final FilesystemCachingConfigurationResource<?> resource) {
        final Path path = getDirectoryForPath(resource.getPath());
        final ResourceWatcher watcher;
        if (!m_watchers.containsKey(path)) {
            watcher = new ResourceWatcher(m_watchService, path);
            m_watchers.put(path, watcher);
        } else {
            watcher = m_watchers.get(path);
        }
        return watcher;
    }

    protected Path getDirectoryForPath(final Path path) {
        if (!Files.isDirectory(path)) {
            return path.getParent();
        }
        return path;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        m_watchServiceMonitor = m_executor.submit(new Runnable() {
            @Override public void run() {
                LOG.debug("Starting WatchService monitoring.");
                while (true) {
                    try {
                        final WatchKey watchKey = m_watchService.poll(1, TimeUnit.SECONDS);
                        if (watchKey == null) continue;
                        LOG.debug("got watchKey: {}", watchKey);

                        for (final WatchEvent<?> event : watchKey.pollEvents()) {
                            final WatchEvent.Kind<?> eventKind = event.kind();
                            if(eventKind == OVERFLOW){
                                LOG.warn("Directory watcher event queue overflowed. {}", event);
                                continue;
                            }

                            @SuppressWarnings("unchecked")
                            final WatchEvent<Path> eventPath = (WatchEvent<Path>)event;
                            final Path filePath = eventPath.context();
                            LOG.debug("Event {} occurred on {}", eventKind, filePath);
                        }
                        for (final ResourceWatcher watcher : m_watchers.values()) {
                            watcher.invalidate();
                        }
                        watchKey.reset();
                    } catch (final InterruptedException e) {
                        tearDown();
                        Thread.currentThread().interrupt();
                    }
                }
            }

            public void tearDown() {
                LOG.debug("WatchService monitor is shutting down.");
                for (final ResourceWatcher watcher : m_watchers.values()) {
                    watcher.cancel();
                }
                try {
                    m_watchService.close();
                } catch (final IOException e) {
                    LOG.warn("Failed to close WatchService.", e);
                }
                m_executor.shutdown();
            }
        });
    }
}
