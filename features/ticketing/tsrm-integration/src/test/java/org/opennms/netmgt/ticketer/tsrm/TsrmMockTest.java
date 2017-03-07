/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.tsrm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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

        when(port.createSHSIMPINC(argThat(new CreateIncidentArg()))).thenReturn(incidentResponse);
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

        when(port.querySHSIMPINC(argThat(new QueryIncidentArg()))).thenReturn(queryResponse);
        port.querySHSIMPINC(queryIncident);

        Ticket ticket = m_ticketer.get(INCIDENT_ID);

        verify(port).querySHSIMPINC(queryIncident);

        assertEquals(ticket.getId(), INCIDENT_ID);
    }

    static class CreateIncidentArg
            extends ArgumentMatcher<CreateSHSIMPINCType> {

        @Override
        public boolean matches(Object argument) {
            return true;
        }
    }

    static class QueryIncidentArg
            extends ArgumentMatcher<QuerySHSIMPINCType> {

        @Override
        public boolean matches(Object argument) {
            return true;
        }
    }

}
