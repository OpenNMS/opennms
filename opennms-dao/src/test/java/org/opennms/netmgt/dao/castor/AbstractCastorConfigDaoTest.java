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
 * 2008 Feb 15: Pass the newly required description argument to AbstractCastorConfigDao's constructor. - dj@opennms.org
 * 2007 Apr 10: Created this file based upon some of the tests in
 *              DefaultSurveillanceViewConfigDao that are common to
 *              AbstractCastorConfigDao. - dj@opennms.org
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
package org.opennms.netmgt.dao.castor;

import java.io.InputStream;

import junit.framework.TestCase;

import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * Unit tests for common AbstractCastorConfigDao functionality.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see AbstractCastorConfigDao
 */
public class AbstractCastorConfigDaoTest extends TestCase {
    public void testAfterPropertiesSetWithNoConfigSet() {
        TestCastorConfigDao dao = new TestCastorConfigDao();
        
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
        TestCastorConfigDao dao = new TestCastorConfigDao();
        dao.setConfigResource(resource);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new CastorDataAccessFailureException(ThrowableAnticipator.IGNORE_MESSAGE));
        
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
        TestCastorConfigDao dao = new TestCastorConfigDao();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("users.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        assertNotNull("userinfo should not be null", dao.getUserinfo());
    }

    public static class TestCastorConfigDao extends AbstractCastorConfigDao<Userinfo, Userinfo> {
        public TestCastorConfigDao() {
            super(Userinfo.class, "user information configuration");
        }

        @Override
        public Userinfo translateConfig(Userinfo castorConfig) {
            return castorConfig;
        }
        
        public Userinfo getUserinfo() {
            return getContainer().getObject();
        }
    }
}
