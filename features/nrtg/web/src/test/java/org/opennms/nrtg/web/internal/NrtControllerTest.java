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