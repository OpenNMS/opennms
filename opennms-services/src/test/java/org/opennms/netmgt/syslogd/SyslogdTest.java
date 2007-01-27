// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
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
//
// $Id: TrapdTest.java 3132 2006-04-16 01:34:14 +0000 (Sun, 16 Apr 2006) mhuot
// $
//

package org.opennms.netmgt.syslogd;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.test.ConfigurationTestUtils;

import java.io.Reader;

public class SyslogdTest extends OpenNMSTestCase {

    private static Syslogd m_syslogd = new Syslogd();

    private Reader rdr;

    protected void setUp() throws Exception {

        rdr = ConfigurationTestUtils.getReaderForResource(this,
                                                          "/org/opennms/netmgt/config/syslogd-configuration.xml");
        super.setUp();
        assertNotNull(DataSourceFactory.getInstance());
        SyslogdConfigFactory.setInstance(new SyslogdConfigFactory(rdr));
        m_syslogd = new Syslogd();
        m_syslogd.init();
        // m_syslogd.start();
    }

    public void tearDown() throws Exception {
        m_syslogd.stop();
       // m_syslogd = null;

        rdr.close();

        super.tearDown();
    }

    public void testMyGrouping() {

        assertEquals(
                     1,
                     SyslogdConfigFactory.getInstance().getMatchingGroupHost());

    }

    public void testMyMessageGroup() {

        assertEquals(
                     2,
                     SyslogdConfigFactory.getInstance().getMatchingGroupMessage());

    }

    public void testPattern() {
        assertEquals(
                     "^Forwarded from (\\\\d+\\\\.\\\\d+\\\\.\\\\d+\\\\.\\\\d+): (.*)",
                     SyslogdConfigFactory.getInstance().getForwardingRegexp());
    }

    public void testDoNothing() {

    }
}
