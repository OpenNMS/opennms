/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.config.service.util;

import static org.opennms.features.config.service.util.PropertiesConversionUtil.mapToJsonString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A direct replacement for org.opennms.core.utils.PropertiesCache.
 * It operates on cm instead of a file.
 */
public class PropertiesCacheWithCm {

  public static final String CHECK_LAST_MODIFY_STRING = "org.opennms.utils.propertiesCache.enableCheckFileModified";
  public static final String CACHE_TIMEOUT = "org.opennms.utils.propertiesCache.cacheTimeout";
  public static final int DEFAULT_CACHE_TIMEOUT = 3600;

  protected static class PropertiesHolder {
    private Properties m_properties;
    private final ConfigurationManagerService cm;
    private final ConfigUpdateInfo configKey;
    private final Lock lock = new ReentrantLock();
    private boolean needReload = false;

    PropertiesHolder(final ConfigurationManagerService cm, ConfigUpdateInfo configKey) {
      this.cm = cm;
      this.configKey = configKey;
      boolean shouldListenToConfigChanges = Boolean.getBoolean(CHECK_LAST_MODIFY_STRING);
      if(shouldListenToConfigChanges) {
        // we want to be notified when config has changed
        cm.registerReloadConsumer(configKey, (ConfigUpdateInfo key) -> this.needReload = true);
      }
      m_properties = null;
    }

    public void requestReload() {
      this.needReload = true;
    }

    private Properties read() {
      Optional<Properties> result = null;
      try {
        result = this.cm.getJSONStrConfiguration(configKey.getConfigName(), configKey.getConfigId())
                .map(PropertiesConversionUtil::jsonToProperties);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (result.isEmpty()) {
        return null;
      }

      Properties props = new Properties();
      props.putAll(result.get());
      needReload = false;

      return props;
    }

    private void write() throws IOException {
      Map<String, Object> entries = new HashMap<>();
      for(Entry<?, ?> entry : this.m_properties.entrySet()) {
        entries.put(entry.getKey().toString(), entry.getValue());
      }
      this.cm.updateConfiguration(this.configKey.getConfigName(), this.configKey.getConfigId(), mapToJsonString(entries));
    }

    public Properties get() throws IOException {
      lock.lock();
      try {
        if (m_properties == null) {
          readWithDefault(new Properties());
        } else {
          if (needReload) {
            m_properties = read();
          }
        }
        return m_properties;
      } finally {
        lock.unlock();
      }
    }

    private void readWithDefault(final Properties deflt) {
      // this is
      // TODO: Patrick: do we need this?
//      if (deflt == null && !m_file.canRead()) {
//        // nothing to load so m_properties remains null no writing necessary
//        // just return to avoid getting the write lock
//        return;
//      }

      if (m_properties == null) {
        m_properties = read();
        if (m_properties == null) {
          m_properties = deflt;
        }
      }
    }

    public void put(final Properties properties) throws IOException {
      lock.lock();
      try {
        m_properties = properties;
        write();
      } finally {
        lock.unlock();
      }
    }

    public void update(final Map<String, String> props) throws IOException {
      if (props == null) return;
      lock.lock();
      try {
        boolean save = false;
        for(Entry<String, String> e : props.entrySet()) {
          if (!e.getValue().equals(get().get(e.getKey()))) {
            get().put(e.getKey(), e.getValue());
            save = true;
          }
        }
        if (save) {
          write();
        }
      } finally {
        lock.unlock();
      }
    }

    public void setProperty(final String key, final String value) throws IOException {
      lock.lock();
      try {
        // first we do get to make sure the properties are loaded
        get();
        if (!value.equals(get().get(key))) {
          get().put(key, value);
          write();
        }
      } finally {
        lock.unlock();
      }
    }

    public Properties find() throws IOException {
      lock.lock();
      try {
        if (m_properties == null) {
          readWithDefault(null);
        }
        return m_properties;
      } finally {
        lock.unlock();
      }
    }

    public String getProperty(final String key) throws IOException {
      lock.lock();
      try {
        return get().getProperty(key);
      } finally {
        lock.unlock();
      }

    }
  }

  protected final Cache<ConfigUpdateInfo, PropertiesHolder> m_cache;

  private final ConfigurationManagerService cm;

  public PropertiesCacheWithCm(final ConfigurationManagerService cm) {
    this(CacheBuilder.newBuilder(), cm);
  }


  protected PropertiesCacheWithCm(final CacheBuilder<Object, Object> cacheBuilder, final ConfigurationManagerService cm) {
    m_cache = cacheBuilder
        .expireAfterAccess(SystemProperties.getInteger(CACHE_TIMEOUT, DEFAULT_CACHE_TIMEOUT), TimeUnit.SECONDS)
        .build();
    this.cm = cm;
  }

  private PropertiesHolder getHolder(final ConfigUpdateInfo configKey) throws IOException {

    try {
      return m_cache.get(configKey, () -> new PropertiesHolder(this.cm, configKey));
    } catch (final ExecutionException e) {
      throw new IOException("Error creating PropertyHolder instance", e);
    }
  }

  /**
   * Get the current properties object from the cache loading it in memory
   */
  public Properties getProperties(final ConfigUpdateInfo configKey) throws IOException {
    return getHolder(configKey).get();
  }

  public Properties findProperties(final ConfigUpdateInfo configKey) throws IOException {
    return getHolder(configKey).find();
  }

  public void saveProperties(final ConfigUpdateInfo configKey, final Properties properties) throws IOException {
    getHolder(configKey).put(properties);
  }

  public void saveProperties(final ConfigUpdateInfo configKey, final Map<String, String> attributeMappings) throws IOException {
    if (attributeMappings == null) return;
    final Properties properties = new Properties();
    properties.putAll(attributeMappings);
    saveProperties(configKey, properties);
  }

  public void updateProperties(final ConfigUpdateInfo configKey, final Map<String, String> props) throws IOException {
    getHolder(configKey).update(props);
  }

  public void setProperty(final ConfigUpdateInfo configKey, final String key, final String value) throws IOException {
    getHolder(configKey).setProperty(key, value);
  }

  public String getProperty(final ConfigUpdateInfo configKey, final String key) throws IOException {
    return getHolder(configKey).getProperty(key);
  }
}
