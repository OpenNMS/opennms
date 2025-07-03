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

import org.springframework.beans.factory.BeanFactory;

/**
 * OpenNMS replacement for Spring's removed BeanFactoryReference interface.
 * 
 * This interface provides the same functionality as Spring's deprecated
 * org.springframework.beans.factory.access.BeanFactoryReference which was
 * removed in Spring 5.0.
 */
public interface BeanFactoryReference {
    
    /**
     * Return the BeanFactory instance held by this reference.
     * @return the BeanFactory instance (never null)
     */
    BeanFactory getFactory();
    
    /**
     * Indicate that this reference is not needed anymore.
     * This may trigger cleanup of the underlying BeanFactory instance.
     */
    void release();
}