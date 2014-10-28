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

package org.opennms.nrtg.web.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.opennms.nrtg.web.internal.NrtController.MetricTuple;

/**
 *
 * @author Markus Neumann
 */
public class NrtControllerTest {
    
    private NrtController nrtController;
    
    @Before
    public void setup() {
        nrtController = new NrtController();
    }
    
    @Test
    public void getMetricIdsByProtocolTest() {
        Map<String, String> rrdGraphAttributesMetaData = new HashMap<String, String>();
        
        //Protocol_metricId=RrdGraphAttribute
        //SNMP_.1.3.6.1.2.1.5.7.0=icmpInRedirects
        //TCA_.1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundJitter=inboundJitter
        
        rrdGraphAttributesMetaData.put("icmpInRedirects", "SNMP_.1.3.6.1.2.1.5.7.0=icmpInRedirects");
        
        rrdGraphAttributesMetaData.put("ifOutOctets", "SNMP_.1.3.6.1.2.1.2.2.1.16.3=ifOutOctets");
        rrdGraphAttributesMetaData.put("ifInOctets", "SNMP_.1.3.6.1.2.1.2.2.1.10.3=ifInOctets");
        
        rrdGraphAttributesMetaData.put("inboundJitter", "TCA_.1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundJitter=inboundJitter");
        Map<String, List<MetricTuple>> metricIdsByProtocol = nrtController.getMetricIdsByProtocol(rrdGraphAttributesMetaData);
        
        assertNotNull(metricIdsByProtocol.get("SNMP"));
        List<MetricTuple> snmpMetrics = metricIdsByProtocol.get("SNMP");
        assertEquals(".1.3.6.1.2.1.2.2.1.16.3", snmpMetrics.get(0).getMetricId());
        
        assertNotNull(metricIdsByProtocol.get("TCA"));
        List<MetricTuple> tcaMetrics = metricIdsByProtocol.get("TCA");
        assertEquals(".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundJitter", tcaMetrics.get(0).getMetricId());
    }
}