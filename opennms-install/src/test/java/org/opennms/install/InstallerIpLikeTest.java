/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
