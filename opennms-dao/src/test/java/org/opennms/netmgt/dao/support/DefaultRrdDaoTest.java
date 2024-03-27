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
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.DefaultRrdGraphDetails;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultRrdDaoTest {
    private RrdStrategy<?,?> m_rrdStrategy = mock(RrdStrategy.class);
    
    private DefaultRrdDao m_dao;
    
    @Before
    public void setUp() throws Exception {
        m_dao = new DefaultRrdDao();
        m_dao.setRrdStrategy(m_rrdStrategy);
        m_dao.setRrdBaseDirectory(new File(System.getProperty("java.io.tmpdir")));
        m_dao.setRrdBinaryPath("/bin/true");
        m_dao.afterPropertiesSet();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_rrdStrategy);
    }

    @Test
    public void testPrintValue() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        OnmsResource childResource = preparePrintValueTest(start, end, "1");

        final var attr = childResource.getAttributes().iterator().next();
        Double value = m_dao.getPrintValue(attr, "AVERAGE", start, end);

        verify(m_rrdStrategy, times(1)).createGraphReturnDetails(anyString(), eq(m_dao.getRrdBaseDirectory()));


        assertNotNull("value should not be null", value);
        assertEquals("value", Double.valueOf(1.0), value);
    }

    @Test
    public void testPrintValueWithNaN() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        OnmsResource childResource = preparePrintValueTest(start, end, "NaN");

        Double value = m_dao.getPrintValue(childResource.getAttributes().iterator().next(), "AVERAGE", start, end);

        verify(m_rrdStrategy, times(1)).createGraphReturnDetails(anyString(), eq(m_dao.getRrdBaseDirectory()));

        assertNotNull("value should not be null", value);
        assertEquals("value", Double.valueOf(Double.NaN), value);
    }
    
    @Test
    public void testPrintValueWithnan() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        OnmsResource childResource = preparePrintValueTest(start, end, "nan");   

        Double value = m_dao.getPrintValue(childResource.getAttributes().iterator().next(), "AVERAGE", start, end);

        verify(m_rrdStrategy, times(1)).createGraphReturnDetails(anyString(), eq(m_dao.getRrdBaseDirectory()));

        assertNotNull("value should not be null", value);
        assertEquals("value", Double.valueOf(Double.NaN), value);
    }

    // NMS-5275
    @Test
    public void testPrintValueWithNegativeNan() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        OnmsResource childResource = preparePrintValueTest(start, end, "-nan");   

        Double value = m_dao.getPrintValue(childResource.getAttributes().iterator().next(), "AVERAGE", start, end);

        verify(m_rrdStrategy, times(1)).createGraphReturnDetails(anyString(), eq(m_dao.getRrdBaseDirectory()));

        assertNotNull("value should not be null", value);
        assertEquals("value", Double.valueOf(Double.NaN), value);
    }

    @Test
    public void testPrintValueWithBogusLine() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String printLine = "blah blah blah this should be a floating point number blah blah blah";
        
        OnmsResource childResource = preparePrintValueTest(start, end, printLine);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new DataAccessResourceFailureException("Value of line 1 of output from RRD is not a valid floating point number: '" + printLine + "'"));
        try {
            m_dao.getPrintValue(childResource.getAttributes().iterator().next(), "AVERAGE", start, end);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        verify(m_rrdStrategy, times(1)).createGraphReturnDetails(anyString(), eq(m_dao.getRrdBaseDirectory()));

        ta.verifyAnticipated();
    }

    private OnmsResource preparePrintValueTest(long start, long end, String printLine) throws IOException, RrdException {
        String rrdDir = "snmp" + File.separator + "1" + File.separator + "eth0";
        String rrdFile = "ifInOctets.jrb";
        
        String escapedFile = rrdDir + File.separator + rrdFile;
        if  (File.separatorChar == '\\') {
        	escapedFile = escapedFile.replace("\\", "\\\\");
        }

        String[] command = new String[] {
                m_dao.getRrdBinaryPath(),
                "graph",
                "-",
                "--start=" + (start / 1000),
                "--end=" + (end / 1000),
                "DEF:ds1=\"" + escapedFile + "\":ifInOctets:AVERAGE",
                "PRINT:ds1:AVERAGE:\"%le\""
        };
        String commandString = StringUtils.arrayToDelimitedString(command, " ");

        OnmsResource topResource = new OnmsResource("1", "Node One", new MockResourceType(), new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));

        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", rrdDir, rrdFile);
        HashSet<OnmsAttribute> attributeSet = new HashSet<OnmsAttribute>(1);
        attributeSet.add(attribute);
        
        MockResourceType childResourceType = new MockResourceType();
        OnmsResource childResource = new OnmsResource("eth0", "Interface One: eth0", childResourceType, attributeSet, new ResourcePath("foo"));
        childResource.setParent(topResource);
        
        DefaultRrdGraphDetails details = new DefaultRrdGraphDetails();
        details.setPrintLines(new String[] { printLine });

        when(m_rrdStrategy.createGraphReturnDetails(commandString, m_dao.getRrdBaseDirectory())).thenReturn(details);

        return childResource;
    }
    
    @Test
    public void testFetchLastValue() throws Exception {
        String rrdDir = "snmp" + File.separator + "1" + File.separator + "eth0";
        String rrdFile = "ifInOctets.jrb";

        OnmsResource topResource = new OnmsResource("1", "Node One", new MockResourceType(), new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));

        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", rrdDir, rrdFile);
        HashSet<OnmsAttribute> attributeSet = new HashSet<OnmsAttribute>(1);
        attributeSet.add(attribute);
        
        MockResourceType childResourceType = new MockResourceType();
        OnmsResource childResource = new OnmsResource("eth0", "Interface One: eth0", childResourceType, attributeSet, new ResourcePath("foo"));
        childResource.setParent(topResource);
        
        int interval = 300000;
        Double expectedValue = Double.valueOf(1.0);
        
        String fullRrdFilePath = m_dao.getRrdBaseDirectory().getAbsolutePath() + File.separator + rrdDir + File.separator + rrdFile;

        when(m_rrdStrategy.fetchLastValue(fullRrdFilePath, attribute.getName(), interval)).thenReturn(expectedValue);

        Double value = m_dao.getLastFetchValue(attribute, interval);
        
        verify(m_rrdStrategy, times(1)).fetchLastValue(getRrdPath(attribute), attribute.getName(), interval);

        assertNotNull("last fetched value must not be null, but was null", value);
        assertEquals("last fetched value", expectedValue, value);
    }
    
    @Test
    public void testFetchLastValueInRange() throws Exception {
        String rrdDir = "snmp" + File.separator + "1" + File.separator + "eth0";
        String rrdFile = "ifInOctets.jrb";

        OnmsResource topResource = new OnmsResource("1", "Node One", new MockResourceType(), new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));

        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", rrdDir, rrdFile);
        HashSet<OnmsAttribute> attributeSet = new HashSet<OnmsAttribute>(1);
        attributeSet.add(attribute);
        
        MockResourceType childResourceType = new MockResourceType();
        OnmsResource childResource = new OnmsResource("eth0", "Interface One: eth0", childResourceType, attributeSet, new ResourcePath("foo"));
        childResource.setParent(topResource);
        
        int interval = 300000;
        int range = 300000;
        Double expectedValue = Double.valueOf(1.0);
        
        String fullRrdFilePath = m_dao.getRrdBaseDirectory().getAbsolutePath() + File.separator + rrdDir + File.separator + rrdFile;

        when(m_rrdStrategy.fetchLastValueInRange(fullRrdFilePath, attribute.getName(), interval, range)).thenReturn(expectedValue);

        Double value = m_dao.getLastFetchValue(attribute, interval, range);

        verify(m_rrdStrategy, times(1)).fetchLastValueInRange(getRrdPath(attribute), attribute.getName(), interval, range);

        assertNotNull("last fetched value must not be null, but was null", value);
        assertEquals("last fetched value", expectedValue, value);
    }

    private String getRrdPath(final OnmsAttribute attr) {
        return m_dao.getRrdBaseDirectory().toPath().toAbsolutePath().resolve(((RrdGraphAttribute)attr).getRrdRelativePath()).toString();
    }
    }
