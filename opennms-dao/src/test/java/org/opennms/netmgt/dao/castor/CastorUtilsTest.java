/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.users.Userinfo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

/**
 * Test class for CastorUtils.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class CastorUtilsTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    public void testUnmarshalReader() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getInputStreamForConfigFile("users.xml"));
    }

    public void testUnmarshalResource() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("users.xml")));
    }
    
    public void testExceptionContainsFileNameUnmarshalResourceWithBadResource() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        /*
         * We are going to attempt to unmarshal groups.xml with the wrong
         * class so we get a MarshalException and we can then test to see if the
         * file name is embedded in the exception.
         */
        boolean gotException = false;
        File file = ConfigurationTestUtils.getFileForConfigFile("groups.xml");
        try {
            CastorUtils.unmarshal(Userinfo.class, new FileSystemResource(file));
        } catch (MarshalException e) {
            String matchString = file.getAbsolutePath().replace('\\', '/');
            if (e.toString().contains(matchString)) {
                gotException = true;
            } else {
                AssertionFailedError ae = new AssertionFailedError("Got an exception, but not one containing the message we were expecting ('" + matchString + "'): " + e);
                ae.initCause(e);
                throw ae;
            }
        }
        
        if (!gotException) {
            fail("Did not get a MarshalException, but we were expecting one.");
        }
    }
    
    public void testUnmarshalInputStreamQuietly() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getInputStreamForConfigFile("users.xml"));
        
        /*
         * Ensure that nothing was logged.
         * In particular, we want to make sure that we don't see this message:
         * 2008-07-28 16:04:53,260 DEBUG [main] org.exolab.castor.xml.Unmarshaller: *static* unmarshal method called, this will ignore any mapping files or changes made to an Unmarshaller instance.
         */
        MockLogAppender.assertNotGreaterOrEqual(Level.DEBUG);
    }
    
    public void testUnmarshalReaderQuietly() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getInputStreamForConfigFile("users.xml"));
        
        /*
         * Ensure that nothing was logged.
         * In particular, we want to make sure that we don't see this message:
         * 2008-07-28 16:04:53,260 DEBUG [main] org.exolab.castor.xml.Unmarshaller: *static* unmarshal method called, this will ignore any mapping files or changes made to an Unmarshaller instance.
         */
        MockLogAppender.assertNotGreaterOrEqual(Level.DEBUG);
    }
    
    public void testUnmarshallInputStreamWithUtf8() throws MarshalException, ValidationException, IOException {
        Userinfo users = CastorUtils.unmarshal(Userinfo.class, ConfigurationTestUtils.getInputStreamForResource(this, "/users-utf8.xml"));
        
        assertEquals("user count", 1, users.getUsers().getUserCount());
        // \u00f1 is unicode for n~ 
        assertEquals("user name", "Admi\u00f1istrator", users.getUsers().getUser(0).getFullName());
    }
    
    public void testUnmarshallResourceWithUtf8() throws MarshalException, ValidationException, IOException {
        Userinfo users = CastorUtils.unmarshal(Userinfo.class, new InputStreamResource(ConfigurationTestUtils.getInputStreamForResource(this, "/users-utf8.xml")));
        
        assertEquals("user count", 1, users.getUsers().getUserCount());
        // \u00f1 is unicode for n~ 
        assertEquals("user name", "Admi\u00f1istrator", users.getUsers().getUser(0).getFullName());
    }
}
