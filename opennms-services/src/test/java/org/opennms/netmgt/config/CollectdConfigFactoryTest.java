//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 10: Simplify setUp() a bit. - dj@opennms.org
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
package org.opennms.netmgt.config;

import java.io.Reader;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.test.ConfigurationTestUtils;

public class CollectdConfigFactoryTest extends TestCase {

    private CollectdConfigFactory m_factory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockNetwork network = new MockNetwork();

        MockDatabase db = new MockDatabase();
        db.populate(network);

        DataSourceFactory.setInstance(db);

        Reader rdr = ConfigurationTestUtils.getReaderForResource(this, "collectd-testdata.xml");
        try {
            m_factory = new CollectdConfigFactory(rdr, "localhost", false);
        } finally {
            rdr.close();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testGetName() {
        String pkgName = "example1";
        CollectdPackage wpkg = m_factory.getPackage(pkgName);
        assertNotNull(wpkg);
        assertEquals(pkgName, wpkg.getName());
    }

}
