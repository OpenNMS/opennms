package org.opennms.web.graph;

import java.io.File;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.dao.DataAccessResourceFailureException;

public class FileReloadContainer<T> {
    private T m_object;
    private File m_file;
    private long m_lastModified;
    private FileReloadCallback<T> m_callback;
    
    public FileReloadContainer(T object, File file,
                               FileReloadCallback<T> callback) {
        m_object = object;
        m_file = file;
        m_callback = callback;
        
        m_lastModified = m_file.lastModified();
    }
    
    public FileReloadContainer(T object) {
        m_object = object;
    }
    
    public T getObject() {
        checkForUpdates();
        return m_object;
    }
    
    private synchronized void checkForUpdates() {
        if (m_file == null) {
            return;
        }
        
        long lastModified = m_file.lastModified();
        
        if (lastModified <= m_lastModified) {
            // Not modified
            return;
        }
        
        // Always update the timestamp, even if we have an error
        m_lastModified = lastModified;
            
        T object = null;
        try {
            object = m_callback.reload(m_object, m_file);
        } catch (Throwable t) {
            String message = 
                "Failed reloading data for object '" + m_object
                + "' from file '" + m_file.getAbsolutePath()
                + ".  Unexpected Throwable received while "
                + "issuing reload: " + t.getMessage();
            log().error(message, t);
            throw new DataAccessResourceFailureException(message, t);
        }
        
        if (object == null) {
            log().info("Not updating object for file '"
                       + m_file.getAbsolutePath()
                       + "' due to reload callback returning null");
        } else {
            m_object = object;
        }
    }
    
    public File getFile() {
        return m_file;
    }
    
    private Category log() {
        return ThreadCategory.getInstance();
    }
}