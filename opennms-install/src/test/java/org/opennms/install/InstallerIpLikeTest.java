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
// 2007 Jun 23: Organize imports. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.install;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.test.DaoTestConfigBean;


public class InstallerIpLikeTest {

    private Installer m_installer;

    @Before
    public void setUp() {
        System.setProperty("skip-native", "true");
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.setRelativeHomeDirectory("target/test-classes");
        System.setProperty("install.dir", "target/test-classes");
        System.setProperty("install.etc.dir", "target/test-classes/etc");
        bean.afterPropertiesSet();
        m_installer = new Installer();
    }

    @Test
    @Ignore
    public void testIpLike() throws Exception {
    	String[] args = {"-d", "-i", "-s", "-Z" };
    	m_installer.install(args);
    }
    
}
