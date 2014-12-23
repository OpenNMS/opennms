package org.opennms.core.config.impl;

import java.util.concurrent.ExecutionException;

import org.opennms.core.config.api.CachingConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class AbstractCachingConfigurationResource<T> implements CachingConfigurationResource<T> {
    private static final String CACHE_INSTANCE_NAME = "instance";
    private final LoadingCache<String, T> m_cache;
    protected abstract T load() throws ConfigurationResourceException;
    protected abstract void save(T config) throws ConfigurationResourceException;

    public AbstractCachingConfigurationResource() {
        m_cache = CacheBuilder.newBuilder()
        .maximumSize(1)
        .build(
               new CacheLoader<String, T>() {
                   public T load(final String key) throws ConfigurationResourceException {
                       return AbstractCachingConfigurationResource.this.load();
                   }
               });
    }

    @Override
    public T get() throws ConfigurationResourceException {
        try {
            return m_cache.get(CACHE_INSTANCE_NAME);
        } catch (final RuntimeException|ExecutionException e) {
            Throwables.propagateIfInstanceOf(e, ConfigurationResourceException.class);
            throw new ConfigurationResourceException(e);
        }
    }

    @Override
    public void put(T config) throws ConfigurationResourceException {
        m_cache.put(CACHE_INSTANCE_NAME, config);
        save(config);
        invalidate();
    }

    @Override
    public void invalidate() {
        m_cache.invalidate(CACHE_INSTANCE_NAME);
    }

}
