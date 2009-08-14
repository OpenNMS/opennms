//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.report.availability;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
   AvailabilityReportConfigurationExecutionListener.class,
   TemporaryDatabaseExecutionListener.class,
   DependencyInjectionTestExecutionListener.class
})

@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-availabilityDatabasePopulator.xml"
})

public class AvailiabilityDatabasePopulatorTest {
    
    
    @Autowired
    AvailabilityDatabasePopulator m_dbPopulator;
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    OutageDao m_outageDao;
    
    private Connection m_availConn;

    
    @Before
    public void setUp() throws Exception {
        
        m_dbPopulator.populateDatabase();


    }

    @Test
    public void testAvailabilityDatabase() {
        
        List<OnmsNode> nodes = m_nodeDao.findAll();
        ListIterator<OnmsNode> cleanNodes = nodes.listIterator();
        while (cleanNodes.hasNext()) {
            System.err.println("NODE "+ cleanNodes.next().toString());
        }
        List<OnmsIpInterface> ifs = m_ipInterfaceDao.findAll();
        ListIterator<OnmsIpInterface> cleanIf = ifs.listIterator();
        while (cleanIf.hasNext()) {
            System.err.println("INTERFACE "+ cleanIf.next().toString());
        }
        Assert.assertEquals("node DB count", 2, m_nodeDao.countAll());
        Assert.assertEquals("service DB count", 3, m_serviceTypeDao.countAll());
        Assert.assertEquals("IP interface DB count", 3, m_ipInterfaceDao.countAll());
        Assert.assertEquals("outages DB Count",6 ,m_outageDao.countAll());
        
        

            try {
                DataSourceFactory.init();
                m_availConn = DataSourceFactory.getInstance().getConnection();
                Statement stmt = m_availConn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                                                     ResultSet.CONCUR_READ_ONLY);
                ResultSet srs = stmt.executeQuery("SELECT ifServices.serviceid, service.servicename FROM ifServices, ipInterface, node, " + "service WHERE ((ifServices.nodeid = 1 )" + 
                "AND (ifServices.ipaddr = '192.168.100.1') AND ipinterface.ipaddr = '192.168.100.1' AND ipinterface.isManaged ='M' AND " + 
                "(ifServices.serviceid = service.serviceid) AND (ifservices.status = 'A')) AND node.nodeid = 1 AND node.nodetype = 'A'");
//                ResultSet srs = stmt.executeQuery("SELECT ipInterface.ipaddr, ipInterface.nodeid FROM ipInterface WHERE ipInterface.ipaddr = '192.168.100.1'" );
                Assert.assertTrue("interface results for 192.168.100.2", srs.next());
                Assert.assertEquals(1 ,srs.getInt(1));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail("unable to execute SQL");
            } finally {
                try {
                    m_availConn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        
        
//        Assert.assertEquals("node DB count", 2, m_db.countRows("select * from node"));
//        Assert.assertEquals("service DB count", 3,
//                     m_db.countRows("select * from service"));
//        Assert.assertEquals("ipinterface DB count", 3,
//                     m_db.countRows("select * from ipinterface"));
//        Assert.assertEquals("interface services DB count", 3,
//                     m_db.countRows("select * from ifservices"));
//        // Assert.assertEquals("outages DB count", 3, m_db.countRows("select * from
//        // outages"));
//        Assert.assertEquals(
//                     "ip interface DB count where ipaddr = 192.168.100.1",
//                     1,
//                     m_db.countRows("select * from ipinterface where ipaddr = '192.168.100.1'"));
//        Assert.assertEquals(
//                     "number of interfaces returned from IPLIKE",
//                     3,
//                     m_db.countRows("select * from ipinterface where iplike(ipaddr,'192.168.100.*')"));
    }
    

}
