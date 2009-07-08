/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.service.lifecycle;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.provision.service.tasks.DefaultTaskCoordinator;

/**
 * LifeCycle
 *
 * @author brozow
 */
public interface LifeCycleInstance {

    List<String> getPhaseNames();

    String getName();
    
    LifeCycleInstance setAttribute(String key, Object value);

    Object getAttribute(String key);

    <T> T findAttributeByType(Class<T> clazz);

    <T> T getAttribute(String key, Class<T> type);

    <T> T getAttribute(String key, T defaultValue);

    LifeCycleInstance createNestedLifeCycle(Phase currentPhase, String lifeCycleName);

    void trigger();

    void waitFor() throws InterruptedException, ExecutionException;

    DefaultTaskCoordinator getCoordinator();

    

}
