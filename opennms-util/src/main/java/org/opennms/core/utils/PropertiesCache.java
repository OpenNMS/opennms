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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Caches properties files in order to improve performance.  
 * 
 * @author brozow
 */

public class PropertiesCache {
    
    private static class PropertiesHolder {
        private Properties m_properties;
        private final File m_file;
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock r = rwLock.readLock();
        private final Lock w = rwLock.writeLock();
        
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
            r.lock();
            try {
                if (m_properties == null) {
                    readWithDefault(new Properties());
                }
                return m_properties;
            } finally {
                r.unlock();
            }
        }

        private void readWithDefault(Properties deflt) throws IOException {
            // this is
            if (deflt == null && !m_file.canRead()) {
                // nothing to load so m_properties remains null no writing necessary
                // just return to avoid getting the write lock
                return;
            }
            
            // release the read lock since we need the write lock to update the properties object
            r.unlock();
            w.lock();
            try {
                // check again to make sure that while we were waiting for the lock
                // someone else didn't get it and load things up already
                if (m_properties == null) {
                    m_properties = read();
                    if (m_properties == null) {
                        m_properties = deflt;
                    }
                }
            } finally {
                // use this ordering so we are sure it is allowed and prevents anyone from getting
                // another write lock before we finish
                r.lock();
                w.unlock();
            }
        }
        
        public void put(Properties properties) throws IOException {
            w.lock();
            try {
                m_properties = properties;
                write();
            } finally {
                w.unlock();
            }
        }

        public void update(Map<String, String> props) throws IOException {
            w.lock();
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
                w.unlock();
            }
        }
        
        public void setProperty(String key, String value) throws IOException {
            w.lock();
            try {
                // first we do get to make sure the properties are loaded
                get();
                if (!value.equals(get().get(key))) {
                    get().put(key, value);
                    write();
                }
            } finally {
                w.unlock();
            }
        }

        public Properties find() throws IOException {
            r.lock();
            try {
                if (m_properties == null) {
                    readWithDefault(null);
                }
                return m_properties;
            } finally {
                r.unlock();
            }
        }

        public String getProperty(String key) throws IOException {
            r.lock();
            try {
                return get().getProperty(key);
            } finally {
                r.unlock();
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
    
    public void clear() {
        synchronized (m_cache) {
            m_cache.clear();
        }
    }

    /**
     * Get the current properties object from the cache loading it in memory 
     * @param propFile
     * @return
     * @throws IOException
     */
    public Properties getProperties(File propFile) throws IOException {
        return getHolder(propFile).get();
    }
    
    public Properties findProperties(File propFile) throws IOException {
        return getHolder(propFile).find();
    }
    public void saveProperties(File propFile, Properties properties) throws IOException {
        getHolder(propFile).put(properties);
    }
    
    public void updateProperties(File propFile, Map<String, String> props) throws IOException {
        getHolder(propFile).update(props);
    }
    
    public void setProperty(File propFile, String key, String value) throws IOException {
        getHolder(propFile).setProperty(key, value);
    }
    
    public String getProperty(File propFile, String key) throws IOException {
        return getHolder(propFile).getProperty(key);
    }
    

}
