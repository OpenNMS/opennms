/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import java.io.InputStream;

import junit.framework.TestCase;

import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public class AccessPointMonitorConfigDaoJaxbTest extends TestCase {

    public void testAfterPropertiesSetWithNoConfigSet() {
        AccessPointMonitorConfigDaoJaxb dao = new AccessPointMonitorConfigDaoJaxb();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property configResource must be set and be non-null"));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testAfterPropertiesSetWithBogusFileResource() throws Exception {
        Resource resource = new FileSystemResource("/bogus-file");
        AccessPointMonitorConfigDaoJaxb dao = new AccessPointMonitorConfigDaoJaxb();
        dao.setConfigResource(resource);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new MarshallingResourceFailureException(ThrowableAnticipator.IGNORE_MESSAGE));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
        AccessPointMonitorConfigDaoJaxb dao = new AccessPointMonitorConfigDaoJaxb();

        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("access-point-monitor-configuration.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();

        assertNotNull("access point monitor config should not be null", dao.getConfig());
    }

}
