/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ResourceTypeUtilsTest extends TestCase {
    private FileAnticipator m_fileAnticipator;
    private File m_snmp;
    private File m_node;
    private File m_intf;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_fileAnticipator = new FileAnticipator();
        
        RrdUtils.setStrategy(new NullRrdStrategy());
    }
    
    @Override
    protected void tearDown() {
        m_fileAnticipator.tearDown();
    }
    

    public void testLoadPropertiesNullRrdDirectory() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("rrdDirectory argument must not be null"));
        try {
            //ResourceTypeUtils.loadProperties(null);
            ResourceTypeUtils.getStringProperties(null, "something");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testLoadPropertiesNullRelativePath() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("relativePath argument must not be null"));
        try {
            //ResourceTypeUtils.loadProperties(null);
            ResourceTypeUtils.getStringProperties(new File(""), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testLoadPropertiesEmpty() throws Exception {
        OnmsResource childResource = createResource();
        createPropertiesFile(childResource, "", false);

        Properties p = ResourceTypeUtils.getStringProperties(m_fileAnticipator.getTempDir(), "snmp/1/eth0");
        
        assertNotNull("properties should not be null", p);
        assertEquals("properties size", 0, p.size());
    }
    
    public void testLoadPropertiesNonEmpty() throws Exception {
        OnmsResource childResource = createResource();
        createPropertiesFile(childResource, "foo=bar", false);

        Properties p = ResourceTypeUtils.getStringProperties(m_fileAnticipator.getTempDir(), "snmp/1/eth0");
        
        assertNotNull("properties should not be null", p);
        assertEquals("properties size", 1, p.size());
        assertNotNull("property 'foo' should exist", p.get("foo"));
        assertEquals("property 'foo' value", "bar", p.get("foo"));
    }

    public void testLoadPropertiesDoesNotExist() throws Exception {
        OnmsResource childResource = createResource();
        createPropertiesFile(childResource, "", true);

        Properties p = ResourceTypeUtils.getStringProperties(m_fileAnticipator.getTempDir(), "snmp/1/eth0");
        assertNull("no properties file was created, so the properties object should be null", p);
    }
    
    public void testGetAttributesAtRelativePathWithBogusDirectory() {
        File bogusRrdDirectory = new File("/foo/bogus/blam/cheese/this/really/should/never/exist");
        assertFalse("bogus RRD directory " + bogusRrdDirectory + " should not exist", bogusRrdDirectory.exists());
        ResourceTypeUtils.getAttributesAtRelativePath(bogusRrdDirectory, "also-should-never-exist");
    }


    private OnmsResource createResource() {
        OnmsResource topResource = new OnmsResource("1", "Node One", new MockResourceType(), new HashSet<OnmsAttribute>(0));
        Set<OnmsAttribute> attributes = new HashSet<OnmsAttribute>(1);
        attributes.add(new RrdGraphAttribute("foo", "1/eth0", "foo.jrb"));
        OnmsResource childResource = new OnmsResource("eth0", "Interface eth0", new MockResourceType(), attributes);
        childResource.setParent(topResource);
        return childResource;
    }

    private File createPropertiesFile(OnmsResource childResource, String propertiesContent, boolean onlyCreateParentDirectories) throws IOException {
        m_fileAnticipator.initialize();
        m_snmp = m_fileAnticipator.tempDir(DefaultResourceDao.SNMP_DIRECTORY);
        m_node = m_fileAnticipator.tempDir(m_snmp, childResource.getParent().getName());
        m_intf = m_fileAnticipator.tempDir(m_node, childResource.getName());
        if (onlyCreateParentDirectories) {
            return new File(m_intf, "strings.properties");
        } else {
            return m_fileAnticipator.tempFile(m_intf, "strings.properties", propertiesContent);
        }
    }
    
}
