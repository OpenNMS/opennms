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
package org.opennms.netmgt.provision.service.lifecycle;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.TaskCoordinator;

/**
 * LifeCycle
 *
 * @author brozow
 * @version $Id: $
 */
public interface LifeCycleInstance {

    /**
     * <p>getPhaseNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getPhaseNames();

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();
    
    /**
     * <p>setAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance} object.
     */
    LifeCycleInstance setAttribute(String key, Object value);

    /**
     * <p>getAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object getAttribute(String key);

    /**
     * <p>findAttributeByType</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    <T> T findAttributeByType(Class<T> clazz);

    /**
     * <p>getAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param type a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    <T> T getAttribute(String key, Class<T> type);

    /**
     * <p>getAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param defaultValue a T object.
     * @param <T> a T object.
     * @return a T object.
     */
    <T> T getAttribute(String key, T defaultValue);

    /**
     * <p>createNestedLifeCycle</p>
     *
     * @param currentPhase a {@link org.opennms.core.tasks.BatchTask} object.
     * @param lifeCycleName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance} object.
     */
    LifeCycleInstance createNestedLifeCycle(BatchTask currentPhase, String lifeCycleName);

    /**
     * <p>trigger</p>
     */
    void trigger();

    /**
     * <p>waitFor</p>
     *
     * @throws java.lang.InterruptedException if any.
     * @throws java.util.concurrent.ExecutionException if any.
     */
    void waitFor() throws InterruptedException, ExecutionException;

    /**
     * <p>getCoordinator</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskCoordinator} object.
     */
    TaskCoordinator getCoordinator();

    

}
