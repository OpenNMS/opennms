/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Mar 03: Quiet test with MockLogAppender. - dj@opennms.org
 * 
 * Created April 30, 2008
 *
 * Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.poller.pollables;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.endsWith;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.opennms.netmgt.config.PollerConfig;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.test.mock.MockLogAppender;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;

import junit.framework.TestCase;

public class LatencyStoringServiceMonitorAdaptorTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private PollerConfig m_pollerConfig = m_mocks.createMock(PollerConfig.class);
    private RrdStrategy m_rrdStrategy = m_mocks.createMock(RrdStrategy.class);
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLogAppender.setupLogging();

        RrdTestUtils.initializeNullStrategy();
        RrdUtils.setStrategy(m_rrdStrategy);
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        MockLogAppender.assertNoWarningsOrGreater();
    }

    public void testUpdateRrdWithLocaleThatUsesCommasForDecimals() throws Exception {
        Locale.setDefault(Locale.FRENCH);
        
        // Make sure we actually have a valid test
        NumberFormat nf = NumberFormat.getInstance();
        assertEquals("ensure that the newly set default locale (" + Locale.getDefault() + ") uses ',' as the decimal marker", "1,5", nf.format(1.5));
        
        LatencyStoringServiceMonitorAdaptor adaptor = new LatencyStoringServiceMonitorAdaptor(null, m_pollerConfig, new Package());
        LinkedHashMap<String, Number> map = new LinkedHashMap<String, Number>();
        map.put("cheese", 1.5);
        
        expect(m_pollerConfig.getStep(isA(Package.class))).andReturn(0).anyTimes();
        expect(m_pollerConfig.getRRAList(isA(Package.class))).andReturn(new ArrayList<String>(0));
        
        expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".rrd").anyTimes();
        expect(m_rrdStrategy.createDefinition(isA(String.class), isA(String.class), isA(String.class), anyInt(), isAList(RrdDataSource.class), isAList(String.class))).andReturn(new Object());
        m_rrdStrategy.createFile(isA(Object.class));
        expect(m_rrdStrategy.openFile(isA(String.class))).andReturn(new Object());
        m_rrdStrategy.updateFile(isA(Object.class), isA(String.class), endsWith(":1.5"));
        m_rrdStrategy.closeFile(isA(Object.class));
        
        m_mocks.replayAll();
        adaptor.updateRRD("foo", InetAddress.getLocalHost(), "baz", map);
        m_mocks.verifyAll();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> isAList(Class<T> clazz) {
        return isA(List.class);
    }
}
