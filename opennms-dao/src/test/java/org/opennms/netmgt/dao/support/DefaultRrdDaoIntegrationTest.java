/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdAttributeType;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.test.FileAnticipator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultRrdDaoIntegrationTest extends TestCase {
    private FileAnticipator m_fileAnticipator;

    private RrdStrategy<RrdDef,RrdDb> m_rrdStrategy;

    private DefaultRrdDao m_dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        m_rrdStrategy = new JRobinRrdStrategy();
        
        m_fileAnticipator = new FileAnticipator();
        
        m_dao = new DefaultRrdDao();
        m_dao.setRrdStrategy(m_rrdStrategy);
        m_dao.setRrdBaseDirectory(m_fileAnticipator.getTempDir());
        m_dao.setRrdBinaryPath("/bin/true");
        m_dao.afterPropertiesSet();
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
    }

    
    @Override
    protected void tearDown() throws Exception {
        m_fileAnticipator.tearDown();
        
        super.tearDown();
    }

    
    public void testInit() {
        // Don't do anything... test that the setUp method works
    }

    public void testPrintValue() throws Exception {
        long start = System.currentTimeMillis();
        long end = start + (24 * 60 * 60 * 1000);
        
        OnmsResource topResource = new OnmsResource("1", "Node One", new MockResourceType(), new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));

        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "snmp/1/eth0", "ifInOctets.jrb");
        HashSet<OnmsAttribute> attributeSet = new HashSet<OnmsAttribute>(1);
        attributeSet.add(attribute);
        
        MockResourceType childResourceType = new MockResourceType();
        OnmsResource childResource = new OnmsResource("eth0", "Interface One: eth0", childResourceType, attributeSet,  new ResourcePath("foo"));
        childResource.setParent(topResource);
        
        File snmp = m_fileAnticipator.tempDir(ResourceTypeUtils.SNMP_DIRECTORY);
        File node = m_fileAnticipator.tempDir(snmp, topResource.getName());
        File intf = m_fileAnticipator.tempDir(node, childResource.getName());
        
        RrdDataSource rrdDataSource = new RrdDataSource(attribute.getName(), RrdAttributeType.GAUGE, 600, "U", "U");
        RrdDef def = m_rrdStrategy.createDefinition("test", intf.getAbsolutePath(), attribute.getName(), 600, Collections.singletonList(rrdDataSource), Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        m_rrdStrategy.createFile(def, null);
        File rrdFile = m_fileAnticipator.expecting(intf, attribute.getName() + m_rrdStrategy.getDefaultFileExtension());
        
        RrdDb rrdFileObject = m_rrdStrategy.openFile(rrdFile.getAbsolutePath());
        for (int i = 0; i < 10; i++) {
            m_rrdStrategy.updateFile(rrdFileObject, "test", (start/1000 + 300*i) + ":1");
        }
        m_rrdStrategy.closeFile(rrdFileObject);
        
        Double value = m_dao.getPrintValue(childResource.getAttributes().iterator().next(), "AVERAGE", start, end);
        
        assertNotNull("value should not be null", value);
        assertEquals("value", 1.0, value);
    }
    
    public void testNMS4861() throws Exception
    {
    	//long endTime = 1312775700L;
    	//long endTime = 1312838400L;
    	long endTime = 1312839213L;
    	long startTime = endTime - 86400L;
    	String command = "/sw/bin/rrdtool graph -" +
    			" --imgformat PNG" +
    			" --font DEFAULT:7" +
    			" --font TITLE:10" +
    			" --start " + startTime +
    			" --end " + endTime +
    			" --title=\"Netscreen Memory Utilization\"" +
    			" --units-exponent=0 " + 
    			" --lower-limit=0" + 
    			" DEF:value1=netscreen-host-resources.jrb:NetScrnMemAlloc:AVERAGE" + 
    			" DEF:value1min=netscreen-host-resources.jrb:NetScrnMemAlloc:MIN" + 
    			" DEF:value1max=netscreen-host-resources.jrb:NetScrnMemAlloc:MAX" + 
    			" DEF:value2=netscreen-host-resources.jrb:NetScrnMemLeft:AVERAGE" + 
    			" DEF:value2min=netscreen-host-resources.jrb:NetScrnMemLeft:MIN" + 
    			" DEF:value2max=netscreen-host-resources.jrb:NetScrnMemLeft:MAX" + 
    			" DEF:value3=netscreen-host-resources.jrb:NetScrnMemFrag:AVERAGE" + 
    			" DEF:value3min=netscreen-host-resources.jrb:NetScrnMemFrag:MIN" + 
    			" DEF:value3max=netscreen-host-resources.jrb:NetScrnMemFrag:MAX" + 
    			" LINE2:value1#0000ff:\"1  minute\"" + 
    			" GPRINT:value1:AVERAGE:\"Avg \\: %10.2lf\"" + 
    			" GPRINT:value1:MIN:\"Min \\: %10.2lf\"" + 
    			" GPRINT:value1:MAX:\"Max \\: %10.2lf\\n\"" + 
    			" LINE2:value2#00ff00:\"5  minute\"" + 
    			" GPRINT:value2:AVERAGE:\"Avg \\: %10.2lf\"" + 
    			" GPRINT:value2:MIN:\"Min \\: %10.2lf\"" + 
    			" GPRINT:value2:MAX:\"Max \\: %10.2lf\\n\"" + 
    			" LINE2:value3#ff0000:\"15 minute\"" + 
    			" GPRINT:value3:AVERAGE:\"Avg \\: %10.2lf\"" + 
    			" GPRINT:value3:MIN:\"Min \\: %10.2lf\"" + 
    			" GPRINT:value3:MAX:\"Max \\: %10.2lf\\n\"" + 
    			"";
    	
    	final File workDir = new File("src/test/resources");
    	final RrdGraphDetails details = m_rrdStrategy.createGraphReturnDetails(command, workDir);
    	final File outputFile = File.createTempFile("img", "png");
    	IOUtils.copy(details.getInputStream(), new FileOutputStream(outputFile));
    	
    	
    }
}
