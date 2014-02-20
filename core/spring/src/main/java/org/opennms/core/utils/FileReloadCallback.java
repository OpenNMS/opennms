/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * Supporting interface for {@link FileReloadContainer} for performing a
 * reload callback.
 *
 * @author dj@opennms.org
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
     * @param resource resource for the underlying object that should be
     *  used for reloading
     * @return the new object, or null if the old object should continue
     *  being used
     * @param <T> a T object.
     */
    public T reload(T object, Resource resource) throws IOException;
}
