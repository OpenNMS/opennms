package org.opennms.web.graph;

import java.io.File;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class FileReloadContainer<K, T> {
    private T m_object;
    private File m_file;
    private long m_lastModified;
    private FileReloadCallback<K, T> m_callback;
    private K m_key; 
    
    public FileReloadContainer(T object, File file,
            FileReloadCallback<K, T> callback,
            K key) {
        m_object = object;
        m_file = file;
        m_callback = callback;
        m_key = key;
        
        m_lastModified = m_file.lastModified();
    }
    
    public FileReloadContainer(T object) {
        m_object = object;
    }
    
    public T get() {
        checkForUpdates();
        return m_object;
    }
    
    private synchronized void checkForUpdates() {
        if (m_file == null) {
            return;
        }
        
        long lastModified = m_file.lastModified();
        
        if (lastModified > m_lastModified) {
            try {
                m_object = m_callback.reload(this);
            } catch (Throwable t) {
                log().error("Failed reloading data for key '" + m_key
                            + "' from file '" + m_file.getAbsolutePath()
                            + ".  Throwable received while issuing reload: "
                            + t.getMessage(),
                            t);
            }
        }
    }
    
    public File getFile() {
        return m_file;
    }
    
    public K getKey() {
        return m_key;
    }
    
    private Category log() {
        return ThreadCategory.getInstance();
    }

}
