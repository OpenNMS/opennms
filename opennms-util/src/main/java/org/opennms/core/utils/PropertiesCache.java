/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Caches properties files in order to improve performance.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PropertiesCache {

    public static final String CHECK_LAST_MODIFY_STRING = "org.opennms.utils.propertiesCache.enableCheckFileModified";

    private static class PropertiesHolder {
        private Properties m_properties;
        private final File m_file;
        private final Lock lock = new ReentrantLock();
        private long m_lastModify = 0;
        private boolean m_checkLastModify = Boolean.getBoolean(CHECK_LAST_MODIFY_STRING);

        PropertiesHolder(File file) {
            m_file = file;
            m_properties = null;
        }
        
        private Properties read() throws IOException {
            if (!m_file.canRead()) {
                return null;
            }

            InputStream in = null;
            try {
                in = new FileInputStream(m_file);
                Properties prop = new Properties();
                prop.load(in);
                if (m_checkLastModify) {
                    m_lastModify = m_file.lastModified();
                }
                return prop;
            } finally {
                if (in != null) {
                    try { in.close(); } catch (IOException e) { }
                }
            }
        }
        
        private void write() throws IOException {
            m_file.getParentFile().mkdirs();
            OutputStream out = null;
            try {
                out = new FileOutputStream(m_file);
                m_properties.store(out, null);
            } finally {
                if (out != null) {
                    try { out.close(); } catch(IOException e) {}
                }
            }
        }

        public Properties get() throws IOException {
            lock.lock();
            try {
                if (m_properties == null) {
                    readWithDefault(new Properties());
                } else {
                    if (m_checkLastModify && m_file.canRead() && m_lastModify != m_file.lastModified()) {
                        m_properties = read();
                    }
                }
                return m_properties;
            } finally {
                lock.unlock();
            }
        }

        private void readWithDefault(Properties deflt) throws IOException {
            // this is
            if (deflt == null && !m_file.canRead()) {
                // nothing to load so m_properties remains null no writing necessary
                // just return to avoid getting the write lock
                return;
            }
            
            if (m_properties == null) {
                m_properties = read();
                if (m_properties == null) {
                    m_properties = deflt;
                }
            }   
        }
        
        public void put(Properties properties) throws IOException {
            lock.lock();
            try {
                m_properties = properties;
                write();
            } finally {
                lock.unlock();
            }
        }

        public void update(Map<String, String> props) throws IOException {
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
        
        public void setProperty(String key, String value) throws IOException {
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

        public String getProperty(String key) throws IOException {
            lock.lock();
            try {
                return get().getProperty(key);
            } finally {
                lock.unlock();
            }
            
        }

    }
    
    
    Map<String, PropertiesHolder> m_cache = new TreeMap<String, PropertiesHolder>();
    
    private PropertiesHolder getHolder(File propFile) throws IOException {
        String key = propFile.getCanonicalPath();
        synchronized (m_cache) {
            PropertiesHolder holder = m_cache.get(key);
            if (holder == null) {
                holder = new PropertiesHolder(propFile);
                m_cache.put(key, holder);
            }
            return holder;
        }
    }
    
    /**
     * <p>clear</p>
     */
    public void clear() {
        synchronized (m_cache) {
            m_cache.clear();
        }
    }

    /**
     * Get the current properties object from the cache loading it in memory
     *
     * @param propFile a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     * @return a {@link java.util.Properties} object.
     */
    public Properties getProperties(File propFile) throws IOException {
        return getHolder(propFile).get();
    }
    
    /**
     * <p>findProperties</p>
     *
     * @param propFile a {@link java.io.File} object.
     * @return a {@link java.util.Properties} object.
     * @throws java.io.IOException if any.
     */
    public Properties findProperties(File propFile) throws IOException {
        return getHolder(propFile).find();
    }
    /**
     * <p>saveProperties</p>
     *
     * @param propFile a {@link java.io.File} object.
     * @param properties a {@link java.util.Properties} object.
     * @throws java.io.IOException if any.
     */
    public void saveProperties(File propFile, Properties properties) throws IOException {
        getHolder(propFile).put(properties);
    }
    
    /**
     * <p>updateProperties</p>
     *
     * @param propFile a {@link java.io.File} object.
     * @param props a {@link java.util.Map} object.
     * @throws java.io.IOException if any.
     */
    public void updateProperties(File propFile, Map<String, String> props) throws IOException {
        getHolder(propFile).update(props);
    }
    
    /**
     * <p>setProperty</p>
     *
     * @param propFile a {@link java.io.File} object.
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public void setProperty(File propFile, String key, String value) throws IOException {
        getHolder(propFile).setProperty(key, value);
    }
    
    /**
     * <p>getProperty</p>
     *
     * @param propFile a {@link java.io.File} object.
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public String getProperty(File propFile, String key) throws IOException {
        return getHolder(propFile).getProperty(key);
    }
    

}
