/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * @author brozow
 *
 */
public class LinkdTopologyProviderFactory {
    
    public static AbstractLinkdTopologyProvider createLinkdTopologyProvider(BlueprintContainer container, ServiceReference<?> enlinkd, ServiceReference<?> linkd) {
        System.err.println("Creating topology provider");
        if (enlinkd != null || linkd == null) {
            System.err.println("returning enhanced topology provider");
            return (AbstractLinkdTopologyProvider)container.getComponentInstance("enhancedLinkdTopologyProviderPrototype");
        } else {
            System.err.println("returning linkd topology provider");
            return (AbstractLinkdTopologyProvider)container.getComponentInstance("linkdTopologyProviderPrototype");
        }

    }
    
    public static String providerLabel(ServiceReference<?> enlinkd, ServiceReference<?> linkd) {
        return (enlinkd != null || linkd == null) ? "Enhanced Linkd" : "Linkd";
    }

}
