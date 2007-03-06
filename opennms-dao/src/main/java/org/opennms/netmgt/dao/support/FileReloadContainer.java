//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.support;

import java.io.File;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>
 * Provides a container for returning an object and reloading the object if
 * an underlying file has changed.  Ideally suited for automatically reloading
 * configuration files that might be edited outside of the application.
 * </p>
 * 
 * <p>
 * <!--
 *      Can't use generics in @see and @link tags.  See Sun bug 5096551:
 *          http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5096551
 *      Leaving off the <T> bits and using Object seem to work okay.
 * -->
 * There are two constructors:
 * <ol>
 *  <li>{@link #FileReloadContainer(Object, File, FileReloadCallback)
 *  FileReloadContainer(T, File, FileReloadCallback&lt;T&gt;)}
 *  is used for objects having an underlying file and are reloadable</li>
 *  <li>{@link #FileReloadContainer(Object) FileReloadContainer(T)} is used
 *  for objects that either do not have an underlying file or are otherwise
 *  not reloadable</li>
 * </ol>
 * The second constructor is provided for convenience so that reloadable and
 * non-reloadable data of the same type can be handled similarly (the only
 * difference in code is at initialization time when one constructor or the
 * other is used).
 * </p>
 * 
 * <p>
 * If the first constructor is used, the File will be stored and
 * {@link File#lastModified() File.lastModified()} will be called every time
 * the {@link #getObject() getObject()} method is called to see if the file
 * has changed.  If the file has changed, the last modified time is updated
 * and the reload callback, {@link FileReloadCallback#reload(Object, File)
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
 *
 * @param <T> the class of the inner object that is stored in this container
 */
public class FileReloadContainer<T> {
    private T m_object;
    private File m_file;
    private long m_lastModified;
    private FileReloadCallback<T> m_callback;
    
    /**
     * Creates a new container with an object and a file underlying that
     * object.  The {@link FileReloadCallback#reload(Object, File) reload}
     * on the callback will be called when the file is modified.
     *  
     * @param object object to be stored in this container
     * @param file file underlying the object
     * @param callback {@link FileReloadCallback#reload(Object, File) reload}
     *  will be called when the underlying file object is modified
     * @throws IllegalArgumentException if object, file, or callback are null
     */
    public FileReloadContainer(T object, File file,
                               FileReloadCallback<T> callback) {
        Assert.notNull(object, "argument object cannot be null");
        Assert.notNull(file, "argument file cannot be null");
        Assert.notNull(callback, "argument callback cannot be null");
        
        m_object = object;
        m_file = file;
        m_callback = callback;
        
        m_lastModified = m_file.lastModified();
    }
    
    /**
     * Creates a new container with an object which has no underlying file.
     * This will not auto-reload.
     * 
     * @param object object to be stored in this container
     * @throws IllegalArgumentException if object is null
     */
    public FileReloadContainer(T object) {
        Assert.notNull(object, "argument object cannot be null");
        
        m_object = object;
    }
    
    /**
     * Get the object in this container.  If the object is backed by a file,
     * the last modified time on the file will be checked, and if it has
     * changed the object will be reloaded.
     * 
     * @return object in this container
     * @throws DataAccessResourceFailureException if an unchecked exception
     *  is received while trying to reload the object from the underlying file
     */
    public T getObject() throws DataAccessResourceFailureException {
        checkForUpdates();
        return m_object;
    }
    
    private synchronized void checkForUpdates()
            throws DataAccessResourceFailureException {
        if (m_file == null) {
            return;
        }
        
        long lastModified = m_file.lastModified();
        
        if (lastModified <= m_lastModified) {
            return;
        }
        
        /*
         * Always update the timestamp, even if we have an error. 
         * XXX What if someone is writing the file while we are reading it,
         * we get an error, and the (correct) file is written completely
         * within the same second, so lastModified doesn't get updated.
         */
        m_lastModified = lastModified;
            
        T object;
        try {
            object = m_callback.reload(m_object, m_file);
        } catch (Throwable t) {
            String message = 
                "Failed reloading data for object '" + m_object + "' "
                + "from file '" + m_file.getAbsolutePath() + "'.  "
                + "Unexpected Throwable received while "
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

    /**
     * Get the file underlying the object in this container, if any.
     * 
     * @return if the container was created with an underlying file the
     *  file will be returned, otherwise null
     */
    public File getFile() {
        return m_file;
    }
    
    private Category log() {
        return ThreadCategory.getInstance();
    }
}