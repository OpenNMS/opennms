/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.spring;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>
 * Provides a container for returning an object and reloading the object if
 * an underlying file has changed.  Ideally suited for automatically reloading
 * configuration files that might be edited outside of the application.
 * </p>
 *
 * There are two constructors:
 * <ol>
 *  <li>{@link #FileReloadContainer(Object, Resource, FileReloadCallback)
 *  FileReloadContainer(T, Resource, FileReloadCallback&lt;T&gt;)}
 *  is used for objects having an underlying resource and are reloadable</li>
 *  <li>{@link #FileReloadContainer(Object) FileReloadContainer(T)} is used
 *  for objects that either do not have an underlying file or are otherwise
 *  not reloadable</li>
 * </ol>
 * The second constructor is provided for convenience so that reloadable and
 * non-reloadable data of the same type can be handled similarly (the only
 * difference in code is at initialization time when one constructor or the
 * other is used).
 *
 * <p>
 * If the first constructor is used, the Resource will be stored for later
 * reloading.  If {@link Resource#getFile() Resource.getFile()} does not
 * throw an exception, the returned File object will be stored and
 * {@link File#lastModified() File.lastModified()} will be called every time
 * the {@link #getObject() getObject()} method is called to see if the file
 * has changed.  If the file has changed, the last modified time is updated
 * and the reload callback, {@link FileReloadCallback#reload(Object, Resource)
 * FileReloadCallback.reload}, is called.  If it returns a non-null object,
 * the new object is stored and it gets returned to the caller.  If a null
 * object is returned, the stored object isn't modified and the old object
 * is returned to the caller.
 * </p>
 *
 * <p>
 * If an unchecked exception is thrown by the reload callback, it will be
 * caught, logged, and a {@link DataAccessResourceFailureException} with
 * a cause of the unchecked exception.  This will propogate up to the
 * caller of the getObject method.  If you do not want unchecked exceptions
 * on reloads to propogate up to the caller of getObject, they need to be
 * caught within the reload method.  Returning a null in the case of errors
 * is a good alternative in this case.
 * </p>
 *
 * @author dj@opennms.org
 * @param <T> the class of the inner object that is stored in this container
 */
public class FileReloadContainer<T> {
	
    private static final Logger LOG = LoggerFactory.getLogger(FileReloadContainer.class);
	
    private static final long DEFAULT_RELOAD_CHECK_INTERVAL = 1000;
    
    private T m_object;
    private Resource m_resource;
    private File m_file;
    private long m_lastModified;
    private long m_lastFileSize;
    private FileReloadCallback<T> m_callback;
    private long m_reloadCheckInterval = DEFAULT_RELOAD_CHECK_INTERVAL;
    private long m_lastReloadCheck;
    private long m_lastUpdate;
    
    /**
     * Creates a new container with an object and a file underlying that
     * object.  If reloadCheckInterval is set to a non-negative value
     * (default is 1000 milliseconds), the last modified timestamp on
     * the file will be checked and the
     * {@link FileReloadCallback#reload(Object, Resource) reload}
     * on the callback will be called when the file is modified.  The
     * check will be performed when {@link #getObject()} is called and
     * at least reloadCheckInterval milliseconds have passed.
     *
     * @param object object to be stored in this container
     * @param callback {@link FileReloadCallback#reload(Object, Resource) reload}
     *  will be called when the underlying file object is modified
     * @throws java.lang.IllegalArgumentException if object, file, or callback are null
     * @param resource a {@link org.springframework.core.io.Resource} object.
     */
    public FileReloadContainer(final T object, final Resource resource, final FileReloadCallback<T> callback) {
        Assert.notNull(object, "argument object cannot be null");
        Assert.notNull(resource, "argument file cannot be null");
        Assert.notNull(callback, "argument callback cannot be null");

        m_object = object;
        m_resource = resource;
        m_callback = callback;
        
        try {
            m_file = resource.getFile();
            m_lastModified = m_file.lastModified();
            m_lastFileSize = m_file.length();
        } catch (final IOException e) {
            // Do nothing... we'll fall back to using the InputStream
            if (LOG.isTraceEnabled()) {
                // if we've got trace turned on, show the stack, but chances are, we don't care about it
                LOG.trace("Resource '{}' does not seem to have an underlying File object; assuming this is not an auto-reloadable file resource", resource, e);
            } else {
                LOG.info("Resource '{}' does not seem to have an underlying File object; assuming this is not an auto-reloadable file resource", resource);
            }
        }
        
        m_lastReloadCheck = System.currentTimeMillis();
    }
    
    public FileReloadContainer(File file, FileReloadCallback<T> callback) {
    	m_object = null;
    	m_resource = new FileSystemResource(file);
    	m_file = file;
    	m_callback = callback;
    	
    	m_lastModified = -1;
    	m_lastFileSize = -1;
    			
    }
    
    /**
     * Creates a new container with an object which has no underlying file.
     * This will not auto-reload.
     *
     * @param object object to be stored in this container
     * @throws java.lang.IllegalArgumentException if object is null
     */
    public FileReloadContainer(final T object) {
        Assert.notNull(object, "argument object cannot be null");
        m_object = object;
    }
    
    /**
     * Get the object in this container.  If the object is backed by a file,
     * the last modified time on the file will be checked, and if it has
     * changed the object will be reloaded.
     *
     * @return object in this container
     * @throws org.springframework.dao.DataAccessResourceFailureException if an unchecked exception
     *  is received while trying to reload the object from the underlying file
     */
    public T getObject() throws DataAccessResourceFailureException {
        checkForUpdates();
        return m_object;
    }
    
    private synchronized void checkForUpdates() throws DataAccessResourceFailureException {
        if (m_file == null || m_reloadCheckInterval < 0 || System.currentTimeMillis() < (m_lastReloadCheck + m_reloadCheckInterval)) {
            return;
        }
        
        m_lastReloadCheck = System.currentTimeMillis();
        
        if (m_file.lastModified() <= m_lastModified && m_file.length() == m_lastFileSize) {
            return;
        }
        
        reload();
    }

    /**
     * Force a reload of the configuration.
     */
    public synchronized void reload() {
        /*
         * Always update the timestamp, even if we have an error. 
         * XXX What if someone is writing the file while we are reading it,
         * we get an error, and the (correct) file is written completely
         * within the same second, so lastModified doesn't get updated.
         */
        m_lastModified = m_file.lastModified();
        m_lastFileSize = m_file.length();
            
        final T object;
        try {
            object = m_callback.reload(m_object, m_resource);
        } catch (Throwable t) {
            final String message = String.format("Failed reloading data for object '%s' from file '%s'. Unexpected Throwable received while issuing reload.", m_object, m_file.getAbsolutePath());
            LOG.error(message, t);
            throw new DataAccessResourceFailureException(message, t);
        }
        
        if (object == null) {
        	LOG.info("Not updating object for file '{}' due to reload callback returning null.", m_file.getAbsolutePath());
        } else {
            m_object = object;
        }
        m_lastUpdate = System.currentTimeMillis();
    }

    /**
     * Get the file underlying the object in this container, if any.
     *
     * @return if the container was created with an underlying file the
     *  file will be returned, otherwise null
     */
    public File getFile() {
        return m_file;
    }

    /**
     * Get the reload check interval.
     *
     * @return reload check interval in milliseconds.  A negative value
     * indicates that automatic reload checks are not performed and the
     * file will only be reloaded if {@link #reload()} is explicitly called.
     */
    public long getReloadCheckInterval() {
        return m_reloadCheckInterval;
    }

    /**
     * Set the reload check interval.
     *
     * @param reloadCheckInterval reload check interval in milliseconds.  A negative value
     * indicates that automatic reload checks are not performed and the
     * file will only be reloaded if {@link #reload()} is explicitly called.
     */
    public void setReloadCheckInterval(final long reloadCheckInterval) {
        m_reloadCheckInterval = reloadCheckInterval;
    }

    /**
     * Get the timestamp in milliseconds of the last time the file was reloaded.
     * 
     * @return the timestamp
     */
    public long getLastUpdate() {
        return m_lastUpdate;
    }
}
