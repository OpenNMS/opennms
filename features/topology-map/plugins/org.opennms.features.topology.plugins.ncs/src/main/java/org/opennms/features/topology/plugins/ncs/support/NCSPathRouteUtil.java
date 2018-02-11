/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.ncs.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.opennms.features.topology.plugins.ncs.NCSServicePath;
import org.opennms.features.topology.plugins.ncs.xpath.JuniperXPath;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class NCSPathRouteUtil {

    
    private NCSComponentRepository m_dao;
    private NodeDao m_nodeDao;

    public NCSPathRouteUtil(NCSComponentRepository dao, NodeDao nodeDao) {
        m_dao = dao;
        m_nodeDao = nodeDao;
    }
    
    public void getServiceName(@JuniperXPath(value="//juniper:ServiceType") String data, Exchange exchange) {
        Message in = exchange.getIn();
        LoggerFactory.getLogger(this.getClass()).info("NCSPathRouteUtil [getServiceName] received message: " + in.toString());
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("serviceType", data);
        header.put("deviceA", in.getHeader("deviceA"));
        header.put("deviceZ", in.getHeader("deviceZ"));
        header.put("foreignId", in.getHeader("foreignId"));
        header.put("foreignSource", in.getHeader("foreignSource"));
        header.put("nodeForeignSource", in.getHeader("nodeForeignSource"));
        header.put("serviceName", in.getHeader("serviceName"));
        exchange.getOut().setHeaders(header);
        LoggerFactory.getLogger(this.getClass()).info("NCSPathRouteUtil send headers: " + exchange.getOut().getHeaders());
    }
    
    public NCSServicePath createPath(@JuniperXPath("//juniper:Data") Node data, Exchange exchange) {
        Message in = exchange.getIn();
        LoggerFactory.getLogger(this.getClass()).info("NCSPathRouteUtil [createPath] received message: " + in.toString());
        String nodeForeignSource = (String) in.getHeader("nodeForeignSource");
        String serviceForeignSource = (String) in.getHeader("foreignSource");
        Node servicePath = data;
        
        String deviceA = (String) in.getHeader("deviceA");
        String deviceZ = (String) in.getHeader("deviceZ");
        String serviceName = (String) in.getHeader("serviceName");
        
        String string = servicePath.getOwnerDocument().getTextContent();
        LoggerFactory.getLogger(this.getClass()).info("NCSPathRouteUtil parsing nodes: " + string);
        return new NCSServicePath(servicePath, m_dao, m_nodeDao, nodeForeignSource, serviceForeignSource, deviceA, deviceZ, serviceName);

    }
}
