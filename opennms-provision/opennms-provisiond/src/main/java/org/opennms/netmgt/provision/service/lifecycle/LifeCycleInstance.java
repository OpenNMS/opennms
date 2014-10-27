/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.lifecycle;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;

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
     * @return a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     */
    DefaultTaskCoordinator getCoordinator();

    

}
