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

package org.opennms.features.topology.plugins.ncs;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.LoggerFactory;

public class NCSPathProviderService {

    private CamelContext m_camelContext;
    private ProducerTemplate m_template;
    
    public NCSPathProviderService(CamelContext camelContext) {
        m_camelContext = camelContext;
        try {
            
            m_template = m_camelContext.createProducerTemplate();
            m_template.start();
            
        } catch (Exception e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception Occurred while creating route: ", e);
        }
        
        
    }
    
    public NCSServicePath getPath(String foreignId, String foreignSource, String deviceAForeignId, String deviceZForeignId, String nodeForeignSource, String serviceName) throws Exception {
        Map<String, Object> headers = new HashMap<String,Object>();
        headers.put("foreignId", foreignId);
        headers.put("foreignSource", foreignSource);
        headers.put("deviceA", deviceAForeignId);
        headers.put("deviceZ", deviceZForeignId);
        headers.put("nodeForeignSource", nodeForeignSource);
        headers.put("serviceName", serviceName);
        
        return m_template.requestBodyAndHeaders("direct:start", null, headers, NCSServicePath.class);
    }
    

}
