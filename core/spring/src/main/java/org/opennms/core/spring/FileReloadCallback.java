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
     */
    public T reload(T object, Resource resource) throws IOException;
}
