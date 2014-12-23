package org.opennms.core.config.api;

/**
 * Additional support for caching semantics on top of the {@link ConfigurationResource} API.
 */
public interface CachingConfigurationResource<T> extends ConfigurationResource<T> {
    /**
     * Mark the cached copy of the resource as dirty, so it is reloaded on the next {@link #get()}.
     */
    public void invalidate();
}
