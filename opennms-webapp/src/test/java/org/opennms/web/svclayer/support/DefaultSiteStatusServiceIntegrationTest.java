//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Sep 10: Don't require config files to be in /opt/OpenNMS/etc. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.svclayer.support;

import java.beans.PropertyVetoException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.C3P0ConnectionFactory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SiteStatusViewsFactory;
import org.opennms.netmgt.config.SurveillanceViewsFactory;
import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class DefaultSiteStatusServiceIntegrationTest extends AbstractTransactionalDataSourceSpringContextTests {
    
    private SiteStatusViewService m_aggregateService;
    
    /*
     *  This is a total hack and should be moved into a Spring bean file
     *  for integration testing.
     */
    public DefaultSiteStatusServiceIntegrationTest() throws MarshalException, ValidationException, IOException, PropertyVetoException, SQLException {
        /*
         * Note: I'm using the opennms-database.xml file in target/classes/etc
         * so that it has been filtered first.  You'll have to make sure that
         * you've done an "install" in opennms-daemon or the top-level,
         * or at least a "compile".
         * 
         * E.g.: ( cd opennms-daemon && ../build.sh compile )
         */
        DataSourceFactory.setInstance(new C3P0ConnectionFactory("../opennms-daemon/target/classes/etc/opennms-database.xml"));

        SurveillanceViewsFactory.setInstance(new SurveillanceViewsFactory("../opennms-daemon/src/main/filtered/etc/surveillance-views.xml"));
        CategoryFactory.setInstance(new CategoryFactory(new FileReader("../opennms-daemon/src/main/filtered/etc/categories.xml")));
        ViewsDisplayFactory.setInstance(new ViewsDisplayFactory("../opennms-daemon/src/main/filtered/etc/viewsdisplay.xml"));
        
        SiteStatusViewsFactory.setInstance(new SiteStatusViewsFactory("../opennms-daemon/src/main/filtered/etc/site-status-views.xml"));
    }
    
    /**
     * This parm gets autowired from the application context by TDSCT (the base class for this test)
     * pretty cool Spring Framework trickery
     * @param svc
     */
    public void setAggregateStatusService(SiteStatusViewService svc) {
        m_aggregateService = svc;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "META-INF/opennms/applicationContext-dao.xml",
                "org/opennms/web/svclayer/applicationContext-svclayer.xml" };
    }
    
    public void testBogus() {
        // Empty test so JUnit doesn't complain about not having any tests to run
    }
    
    public void FIXMEtestCreateAggregateStatusView() {
        
        AggregateStatusView view = m_aggregateService.createAggregateStatusView(null);
        
        assertNotNull(view);
        assertFalse(view.getStatusDefinitions().isEmpty());
    }
    
    public void FIXMEtestCreateAggregateStatusUsingNodeId() {
        Collection<AggregateStatus> aggrStati = m_aggregateService.createAggregateStatusesUsingNodeId(1, "building");
        assertNotNull(aggrStati);
    }
    
    public void FIXMEtestCreateAggregateStatusUsingBuilding() {
        
        Collection<AggregateStatus> aggrStati;
        Collection<AggregateStatusDefinition> defs = new ArrayList<AggregateStatusDefinition>();
        
        AggregateStatusDefinition definition;
        definition = new AggregateStatusDefinition("LB/Router", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_ROUTER"), new OnmsCategory("DEV_LOADBAL") })));
        defs.add(definition);        
        definition = new AggregateStatusDefinition("Access Controller", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_AC") })));
        defs.add(definition);
        definition = new AggregateStatusDefinition("Switches", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_SWITCH") })));
        defs.add(definition);
        definition = new AggregateStatusDefinition("Access Points", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_AP") })));
        defs.add(definition);
        definition = new AggregateStatusDefinition("BCPC", new HashSet<OnmsCategory>(Arrays.asList(new OnmsCategory[]{ new OnmsCategory("DEV_BCPC") })));
        defs.add(definition);
        
        AggregateStatusView view = new AggregateStatusView();
        view.setName("building");
        view.setColumnName("building");
        view.setColumnValue("HAT102706");
        view.setStatusDefinitions(new LinkedHashSet<AggregateStatusDefinition>(defs));
        
        aggrStati = m_aggregateService.createAggregateStatuses(view);
        
        AggregateStatus status;
        status = (AggregateStatus)((ArrayList)aggrStati).get(0);
        assertEquals(status.getStatus(), AggregateStatus.NODES_ARE_DOWN);
        
        status = (AggregateStatus)((ArrayList)aggrStati).get(1);
        assertEquals(status.getStatus(), AggregateStatus.ALL_NODES_UP);
        
        status = (AggregateStatus)((ArrayList)aggrStati).get(2);
        assertEquals(status.getStatus(), AggregateStatus.NODES_ARE_DOWN);

        status = (AggregateStatus)((ArrayList)aggrStati).get(3);
        assertEquals(status.getStatus(), AggregateStatus.NODES_ARE_DOWN);
        assertEquals(new Integer(6), status.getDownEntityCount());
        
        status = (AggregateStatus)((ArrayList)aggrStati).get(4);
        assertEquals(status.getStatus(), AggregateStatus.ALL_NODES_UP);

    }

}
