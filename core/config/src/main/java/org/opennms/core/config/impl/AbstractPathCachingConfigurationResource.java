package org.opennms.core.config.impl;

import java.io.File;
import java.nio.file.Path;

import org.opennms.core.config.api.FilesystemCachingConfigurationResource;
import org.springframework.util.Assert;

public abstract class AbstractPathCachingConfigurationResource<T> extends AbstractCachingConfigurationResource<T> implements FilesystemCachingConfigurationResource<T> {
    private final Path m_path;

    public AbstractPathCachingConfigurationResource(final Path path) {
        Assert.notNull(path);
        m_path = path;
    }

    public AbstractPathCachingConfigurationResource(final File file) {
        Assert.notNull(file);
        m_path = file.toPath();
    }

    @Override
    public Path getPath() {
        return m_path;
    }
}
