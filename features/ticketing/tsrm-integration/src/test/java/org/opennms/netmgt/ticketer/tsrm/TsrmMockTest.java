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
package org.opennms.netmgt.ticketer.tsrm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

import java.io.File;
import java.nio.file.Paths;

import com.ibm.maximo.CreateSHSIMPINCResponseType;
import com.ibm.maximo.CreateSHSIMPINCType;
import com.ibm.maximo.INCIDENTKeyType;
import com.ibm.maximo.INCIDENTMboKeySetType;
import com.ibm.maximo.MXStringType;
import com.ibm.maximo.QuerySHSIMPINCResponseType;
import com.ibm.maximo.QuerySHSIMPINCType;
import com.ibm.maximo.SHSIMPINCINCIDENTType;
import com.ibm.maximo.SHSIMPINCSetType;

import com.ibm.maximo.wsdl.shsimpinc.SHSIMPINCPortType;

public class TsrmMockTest {

    public TsrmTicketerPlugin m_ticketer;
    public SHSIMPINCPortType port;
    private static final String INCIDENT_ID = "1001";

    @Before
    public void setup() {
        final File opennmsHome = Paths.get("src",
                                           "test",
                                           "resources",
                                           "opennms-home").toFile();
        assertTrue(opennmsHome + " must exist.", opennmsHome.exists());
        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());

        m_ticketer = new TsrmTicketerPlugin();
        port = mock(SHSIMPINCPortType.class);
        m_ticketer.setPort(port);
    }

    @Test
    public void testSaveWithMock() throws PluginException {

        CreateSHSIMPINCType createIncidentType = new CreateSHSIMPINCType();

        CreateSHSIMPINCResponseType incidentResponse = new CreateSHSIMPINCResponseType();
        INCIDENTMboKeySetType incidentMboKeyType = new INCIDENTMboKeySetType();
        INCIDENTKeyType incidentKey = new INCIDENTKeyType();
        MXStringType incidentId = new MXStringType();
        incidentId.setValue(INCIDENT_ID);
        incidentKey.setTICKETID(incidentId);
        incidentResponse.setINCIDENTMboKeySet(incidentMboKeyType);
        incidentMboKeyType.getINCIDENT().add(incidentKey);

        when(port.createSHSIMPINC(argThat(new ArgumentMatcher<CreateSHSIMPINCType>() {
            @Override
            public boolean matches(CreateSHSIMPINCType createSHSIMPINCType) {
                return true;
            }
        }))).thenReturn(incidentResponse);
        port.createSHSIMPINC(createIncidentType);

        Ticket ticket = new Ticket();
        m_ticketer.saveOrUpdate(ticket);

        verify(port).createSHSIMPINC(createIncidentType);
        assertEquals(ticket.getId(), INCIDENT_ID);
    }

    @Test
    public void testGetWithMock() throws PluginException {

        QuerySHSIMPINCType queryIncident = new QuerySHSIMPINCType();

        QuerySHSIMPINCResponseType queryResponse = new QuerySHSIMPINCResponseType();
        SHSIMPINCSetType queryType = new SHSIMPINCSetType();
        SHSIMPINCINCIDENTType queryIncidentType = new SHSIMPINCINCIDENTType();
        MXStringType incidentId = new MXStringType();
        incidentId.setValue(INCIDENT_ID);
        queryIncidentType.setTICKETID(incidentId);
        queryType.getINCIDENT().add(queryIncidentType);
        queryResponse.setSHSIMPINCSet(queryType);

        when(port.querySHSIMPINC(argThat(new ArgumentMatcher<QuerySHSIMPINCType>() {
            @Override
            public boolean matches(QuerySHSIMPINCType querySHSIMPINCType) {
                return true;
            }
        }))).thenReturn(queryResponse);
        port.querySHSIMPINC(queryIncident);

        Ticket ticket = m_ticketer.get(INCIDENT_ID);

        verify(port).querySHSIMPINC(queryIncident);

        assertEquals(ticket.getId(), INCIDENT_ID);
    }
}
