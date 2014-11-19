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

package org.opennms.netmgt.ncs.persistence;

import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.ncs.rest.NCSRestService.ComponentList;

public interface NCSComponentService {
	public void setEventProxy(EventProxy eventProxy) throws Exception;

	public NCSComponent getComponent(String type, String foreignSource, String foreignId);
	public ComponentList findComponentsWithAttribute(String string, String string2);

	public NCSComponent addOrUpdateComponents(NCSComponent component, boolean deleteOrphans);
	public NCSComponent addSubcomponent(String type, String foreignSource, String foreignId, NCSComponent subComponent, boolean deleteOrphans);
	public void deleteComponent(String type, String foreignSource, String foreignId, boolean deleteOrphans);
}
