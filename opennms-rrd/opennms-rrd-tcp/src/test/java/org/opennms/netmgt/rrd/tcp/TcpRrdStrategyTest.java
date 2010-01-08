/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jul 29: Fix up a test string. - dj@opennms.org
 * 2008 Jun 17: Add tests for bug #2223. - jeffg@opennms.org
 * 2008 Feb 15: Add tests for bug #2272. - dj@opennms.org
 * 2007 Mar 19: Adjust for changes with exceptions and add test
 *              for a graph with only PRINT commands through the
 *              RrdStrategy interface with createGraphReturnDetails. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.rrd.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.util.StringUtils;

/**
 * Unit tests for the TcpRrdStrategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class TcpRrdStrategyTest {

    private RrdStrategy m_strategy;
    private FileAnticipator m_fileAnticipator;

    @Before
    public void setUp() throws Exception {

        MockLogAppender.setupLogging();

        m_strategy = new TcpRrdStrategy();
        m_strategy.initialize();

        // Don't initialize by default since not all tests need it.
        m_fileAnticipator = new FileAnticipator(false);
    }

    /*
    @After
    public void tearDown() throws Exception {
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
        m_fileAnticipator.tearDown();
    }
     */

    @Test
    public void testInitialize() {
        // Don't do anything... just check that setUp works 
    }

    @Test
    public void testCreate() throws Exception {
        File rrdFile = createRrdFile();

        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        //m_strategy.updateFile(openedFile, "huh?", "N:1,234234");

        m_strategy.closeFile(openedFile);
    }

    @Test
    public void testUpdate() throws Exception {
        File rrdFile = createRrdFile();

        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        m_strategy.updateFile(openedFile, "huh?", "N:1.234234");
        m_strategy.closeFile(openedFile);
    }

    public File createRrdFile() throws Exception {
        String rrdFileBase = "foo";
        String rrdExtension = ".jrb";

        m_fileAnticipator.initialize();

        // This is so the RrdUtils.getExtension() call in the strategy works
        Properties properties = new Properties();
        properties.setProperty("org.opennms.rrd.fileExtension", rrdExtension);
        RrdConfig.setProperties(properties);

        List<RrdDataSource> dataSources = new ArrayList<RrdDataSource>();
        dataSources.add(new RrdDataSource("bar", "GAUGE", 3000, "U", "U"));
        List<String> rraList = new ArrayList<String>();
        rraList.add("RRA:AVERAGE:0.5:1:2016");
        Object def = m_strategy.createDefinition("hello!", m_fileAnticipator.getTempDir().getAbsolutePath(), rrdFileBase, 300, dataSources, rraList);
        m_strategy.createFile(def);

        return m_fileAnticipator.expecting(rrdFileBase + rrdExtension);
    }
}
