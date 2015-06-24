/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.util.Collection;

import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;

/*
 * PluginRegistry
 * @author brozow
 */
/**
 * <p>PluginRegistry interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface PluginRegistry {

    /**
     * <p>getAllPlugins</p>
     *
     * @param pluginClass a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a {@link java.util.Collection} object.
     */
    public abstract <T> Collection<T> getAllPlugins(Class<T> pluginClass);

    /**
     * <p>getPluginInstance</p>
     *
     * @param pluginClass a {@link java.lang.Class} object.
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public abstract <T> T getPluginInstance(Class<T> pluginClass,
            PluginConfig pluginConfig);
    

}
