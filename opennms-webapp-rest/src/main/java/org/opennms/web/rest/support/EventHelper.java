/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import java.util.Date;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;

public class EventHelper {

    public static void updateEvent(Event event, NodeDao nodeDao) {
        if (event.getTime() == null) event.setTime(new Date());
        if (event.getSource() == null) event.setSource("ReST");
        if (event.getNodeid() == 0) {
            org.opennms.netmgt.xml.event.Parm foreignSource = event.getParm("_foreignSource");
            org.opennms.netmgt.xml.event.Parm foreignId = event.getParm("_foreignId");
            if (foreignSource != null && foreignSource.getValue() != null && foreignId != null && foreignId.getValue() != null) {
                OnmsNode node = nodeDao.findByForeignId(foreignSource.getValue().getContent(), foreignId.getValue().getContent());
                if (node != null) {
                    event.setNodeid(Long.parseLong(node.getId().toString()));
                }
            }
        }
    }

}
