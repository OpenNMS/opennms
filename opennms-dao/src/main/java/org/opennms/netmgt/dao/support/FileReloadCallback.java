package org.opennms.netmgt.dao.support;

import java.io.File;

/**
 * Supporting interface for {@link FileReloadContainer} for performing a
 * reload callback.
 *  
 * @author dj@opennms.org
 *
 * @param <T>  the class of the inner object that is stored in the
 *  {@link FileReloadContainer}
 */
public interface FileReloadCallback<T> {
    /**
     * <p>
     * Reload the specified object from the underlying file and return the
     * new object.  This is called when
     * {@link FileReloadContainer#getObject()}
     * determines that the underlying file object has changed.
     * </p>
     * 
     * <p>
     * Any unchecked exceptions that are thrown will be caught by the
     * container, logged, and rethrown with additional details including the
     * object and the file underlying the object.  Note that such unchecked
     * exceptions <b>will</b> propogate back up to the caller of getObject().
     * Unchecked exceptions should be caught if this is not desired.  Null can
     * be returned to indicated to the caller that the old object should
     * continue to be used. 
     * </p>
     * 
     * @param object object to be reloaded.  This is useful if the class
     *  receiving the callback handles many objects of the same type and
     *  needs to know any details about the object being reloaded.
     * @param file file for the underlying object that should be used for
     *  reloading
     * @return the new object, or null if the old object should continue
     *  being used
     */
    public T reload(T object, File file);
}
