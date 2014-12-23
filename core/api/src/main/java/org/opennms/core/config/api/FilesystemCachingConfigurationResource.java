package org.opennms.core.config.api;

import java.nio.file.Path;

public interface FilesystemCachingConfigurationResource<T> extends CachingConfigurationResource<T> {
    /**
     * Get the path to the represented configuration resource.
     */
    public Path getPath();
}
