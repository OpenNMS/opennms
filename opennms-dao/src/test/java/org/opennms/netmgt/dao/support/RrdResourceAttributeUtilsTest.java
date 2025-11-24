/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.PropertiesCache;
import org.opennms.netmgt.dao.support.RrdResourceAttributeUtils.AlphaNumericOnmsAttributeComparator;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class RrdResourceAttributeUtilsTest {
    private FileAnticipator m_fileAnticipator;
    private File m_snmp;
    private File m_node;
    private File m_intf;

    @Before
    public void setUp() throws Exception {
        m_fileAnticipator = new FileAnticipator();
    }

    @After
    public void tearDown() throws Exception {
        m_fileAnticipator.tearDown();
    }

    @Test
    public void testLoadPropertiesNullRrdDirectory() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("rrdDirectory argument must not be null"));
        try {
            RrdResourceAttributeUtils.getStringProperties(null, "something");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testLoadPropertiesNullRelativePath() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("relativePath argument must not be null"));
        try {
            RrdResourceAttributeUtils.getStringProperties(new File(""), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testLoadPropertiesEmpty() throws Exception {
        OnmsResource childResource = createResource();
        createPropertiesFile(childResource, "", false);

        Properties p = RrdResourceAttributeUtils.getStringProperties(m_fileAnticipator.getTempDir(), "snmp/1/eth0");

        assertNotNull("properties should not be null", p);
        assertEquals("properties size", 0, p.size());
    }
    
    @Test
    public void testLoadPropertiesNonEmpty() throws Exception {
        OnmsResource childResource = createResource();
        createPropertiesFile(childResource, "foo=bar", false);

        Properties p = RrdResourceAttributeUtils.getStringProperties(m_fileAnticipator.getTempDir(), "snmp/1/eth0");

        assertNotNull("properties should not be null", p);
        assertEquals("properties size", 1, p.size());
        assertNotNull("property 'foo' should exist", p.get("foo"));
        assertEquals("property 'foo' value", "bar", p.get("foo"));
    }

    @Test
    public void testLoadPropertiesDoesNotExist() throws Exception {
        OnmsResource childResource = createResource();
        createPropertiesFile(childResource, "", true);

        Properties p = RrdResourceAttributeUtils.getStringProperties(m_fileAnticipator.getTempDir(), "snmp/1/eth0");
        assertNull("no properties file was created, so the properties object should be null", p);
    }
    
    @Test
    public void testGetAttributesAtRelativePathWithBogusDirectory() {
        File bogusRrdDirectory = new File("/foo/bogus/blam/cheese/this/really/should/never/exist");
        assertFalse("bogus RRD directory " + bogusRrdDirectory + " should not exist", bogusRrdDirectory.exists());
        RrdResourceAttributeUtils.getAttributesAtRelativePath(bogusRrdDirectory, "also-should-never-exist", ".rrd");
    }

    /*
     * This test is associated with issue NMS-5806:
     * http://issues.opennms.org/browse/NMS-5806
     */
    @Test
    public void testUpdateFileOutsideResourceTypeUtils() throws Exception {
        System.setProperty(PropertiesCache.CHECK_LAST_MODIFY_STRING, "true");

        // Be sure that the file doesn't exist.
        File resourceDir = new File("target/");
        File propertiesFile = new File(resourceDir, "strings.properties");
        propertiesFile.delete();

        // Creating a new strings.properties file and adding one value to it
        RrdResourceAttributeUtils.updateStringProperty(resourceDir, "2012", "year");
        assertEquals("2012", RrdResourceAttributeUtils.getStringProperty(resourceDir, "year"));
        Thread.sleep(1000l); // Simulate a delay, to be sure that we are going to have a different lastModifyTime

        // Externally updating the strings.proeprties file
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        properties.setProperty("year", "2013");
        properties.store(new FileWriter(propertiesFile), "Updated!");

        // Verify that after the external update, we can get the updated value
        assertEquals("2013", RrdResourceAttributeUtils.getStringProperty(resourceDir, "year"));
    }

    @Test
    public void testResourceAttributeSorting() throws Exception {
        final AlphaNumericOnmsAttributeComparator comparator = new AlphaNumericOnmsAttributeComparator();

        List<RrdGraphAttribute> testArray = Arrays.asList(
            new RrdGraphAttribute("abcd123", "abcd123/eth0", "abcd123.rrd"),
            new RrdGraphAttribute("abcd10", "abcd10/eth0", "abcd10.rrd"),
            new RrdGraphAttribute("SOMETHING", "SOMETHING/eth0", "something.rrd"),
            new RrdGraphAttribute("AANYTHING", "AANYTHING/eth0", "aanything.rrd")
        );
        testArray.sort(comparator);
        assertArrayEquals(
            Arrays.asList("AANYTHING", "abcd10", "abcd123", "SOMETHING").toArray(new String[0]),
	        testArray.stream().map(RrdGraphAttribute::getName).collect(Collectors.toList()).toArray(new String[0])
	    );

        testArray = Arrays.asList(
            new RrdGraphAttribute("NMS1234", "NMS1234/eth0", "nms1234.rrd"),
            new RrdGraphAttribute("nms1234", "nms1234/eth0", "nms1234.rrd"),
            new RrdGraphAttribute("this is a test", "test/eth0", "test.rrd"),
            new RrdGraphAttribute("test go brrr", "brrr/eth0", "brrr.rrd")
        );
        testArray.sort(comparator);
        assertArrayEquals(
            Arrays.asList("NMS1234", "nms1234", "test go brrr", "this is a test").toArray(new String[0]),
	        testArray.stream().map(RrdGraphAttribute::getName).collect(Collectors.toList()).toArray(new String[0])
	    );
    }

    private OnmsResource createResource() {
        OnmsResource topResource = new OnmsResource("1", "Node One", new MockResourceType(), new HashSet<OnmsAttribute>(0), new ResourcePath("foo"));
        Set<OnmsAttribute> attributes = new HashSet<OnmsAttribute>(1);
        attributes.add(new RrdGraphAttribute("foo", "1/eth0", "foo.rrd"));
        OnmsResource childResource = new OnmsResource("eth0", "Interface eth0", new MockResourceType(), attributes, new ResourcePath("foo"));
        childResource.setParent(topResource);
        return childResource;
    }

    private File createPropertiesFile(OnmsResource childResource, String propertiesContent, boolean onlyCreateParentDirectories) throws IOException {
        m_fileAnticipator.initialize();
        m_snmp = m_fileAnticipator.tempDir(ResourceTypeUtils.SNMP_DIRECTORY);
        m_node = m_fileAnticipator.tempDir(m_snmp, childResource.getParent().getName());
        m_intf = m_fileAnticipator.tempDir(m_node, childResource.getName());
        if (onlyCreateParentDirectories) {
            return new File(m_intf, "strings.properties");
        } else {
            return m_fileAnticipator.tempFile(m_intf, "strings.properties", propertiesContent);
        }
    }
    
}
