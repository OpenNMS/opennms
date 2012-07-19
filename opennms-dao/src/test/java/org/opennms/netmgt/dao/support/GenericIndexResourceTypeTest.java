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

package org.opennms.netmgt.dao.support;

import static org.easymock.EasyMock.expect;

import java.io.File;

import junit.framework.TestCase;

import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.EasyMockUtils;

public class GenericIndexResourceTypeTest extends TestCase {
    private FileAnticipator m_fileAnticipator;
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private ResourceDao m_resourceDao = m_mocks.createMock(ResourceDao.class);
    private StorageStrategy m_storageStrategy = m_mocks.createMock(StorageStrategy.class);
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        RrdUtils.setStrategy(new NullRrdStrategy());

        // Don't initialize by default since not all tests need it.
        m_fileAnticipator = new FileAnticipator(false);
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        m_fileAnticipator.tearDown();

        super.tearDown();
    }
    
    public void testGetResourceByNodeAndIndexGetLabelPlain() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "plain", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "plain", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndex() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "1", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelStringAttribute() throws Exception {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${stringAttribute}", m_storageStrategy);
        
        m_fileAnticipator.initialize();
        expect(m_resourceDao.getRrdDirectory()).andReturn(m_fileAnticipator.getTempDir());
        
        File snmpDir = m_fileAnticipator.tempDir("snmp");
        File snmpNodeDir = m_fileAnticipator.tempDir(snmpDir, "1");
        File fooDir = m_fileAnticipator.tempDir(snmpNodeDir, "foo");
        File indexDir = m_fileAnticipator.tempDir(fooDir, "1");
        m_fileAnticipator.tempFile(indexDir, DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME, "stringAttribute=hello!!!!");
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "hello!!!!", resource.getLabel());
    }
    
    public void testGetResourceByNodeSourceAndIndexGetLabelStringAttribute() throws Exception {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${stringAttribute}", m_storageStrategy);

        m_fileAnticipator.initialize();
        expect(m_resourceDao.getRrdDirectory()).andReturn(m_fileAnticipator.getTempDir());

        File snmpDir = m_fileAnticipator.tempDir("snmp");
        File forSrcDir = m_fileAnticipator.tempDir(snmpDir, "fs");
        File snmpNodeSourceDir = m_fileAnticipator.tempDir(forSrcDir, "source1");
        File forIdDir = m_fileAnticipator.tempDir(snmpNodeSourceDir, "123");
        File fooDir = m_fileAnticipator.tempDir(forIdDir, "foo");
        File indexDir = m_fileAnticipator.tempDir(fooDir, "1");
        m_fileAnticipator.tempFile(indexDir, DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME, "stringAttribute=hello!!!!");

        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeSourceAndIndex("source1:123", "1");
        m_mocks.verifyAll();

        assertNotNull("resource", resource);
        assertEquals("resource label", "hello!!!!", resource.getLabel());
    }

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexNumber() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(3, 1)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "4", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexBogusArguments() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(absolutely bogus)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${subIndex(absolutely bogus)}", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexBogusOffset() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(foo, 1)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${subIndex(foo, 1)}", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexBadNumber() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(4, 1)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${subIndex(4, 1)}", resource.getLabel());
    }
    
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexBeginning() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(1)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "2.3.4", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexEnding() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(0, 3)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "1.2.3", resource.getLabel());
    }
    

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexNoArguments() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex()}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${subIndex()}", resource.getLabel());
    }
    

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexStartOutOfBounds() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(4)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${subIndex(4)}", resource.getLabel());
    }
    

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexEndOutOfBounds() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${subIndex(0, 5)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${subIndex(0, 5)}", resource.getLabel());
    }

    public void testGetResourceByNodeAndIndexGetLabelIndexWithHexConversion() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${hex(index)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4.14.15");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "01:02:03:04:0E:0F", resource.getLabel());
    }

    public void testGetResourceByNodeAndIndexGetLabelIndexWithHexConversionBogusInteger() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${hex(index)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "foo");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${hex(index)}", resource.getLabel());
    }
    
    /**
     * Test for enhancement in bug #2467.
     */
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubStringAndHexConversion() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "MAC Address ${hex(subIndex(0, 6))} on interface ${subIndex(6, 1)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "0.21.109.80.9.66.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "MAC Address 00:15:6D:50:09:42 on interface 4", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubStringOfDynamicLength() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "Easy as ${subIndex(0, n)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "5.1.2.3.4.5");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "Easy as 1.2.3.4.5", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithThreeSubStringsOfDynamicLength() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "Easy as ${subIndex(0, n)} and ${subIndex(n, n)} and ${subIndex(n, n)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.1.2.1.2.3.1.2.3");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "Easy as 1 and 1.2 and 1.2.3", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubStringAndDynSubStringAndDynSubStringAndSubStringToEnd() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "Easy as ${subIndex(0, 1)} and ${subIndex(1, n)} and ${subIndex(n, n)} and ${subIndex(n)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "3.3.1.2.3.3.4.5.6.0");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "Easy as 3 and 1.2.3 and 4.5.6 and 0", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithDisplaySubStringOfDynamicLength() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "Easy as ${string(subIndex(0, n))}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "3.112.105.101");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "Easy as pie", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubStringAndTwoDisplaySubStringsOfDynamicLengthAndSubStringToEnd() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "Easy as ${subIndex(0, 1)} piece of ${string(subIndex(1, n))} or just under ${string(subIndex(n, n))} pieces of ${subIndex(n)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.3.112.105.101.2.80.105.3.1.4.1.5.9");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "Easy as 1 piece of pie or just under Pi pieces of 3.1.4.1.5.9", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithBogusUseOfNforStartOfFirstSubIndex() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "Easy as ${subIndex(n, 3)}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "3.1.2.3");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "Easy as ${subIndex(n, 3)}", resource.getLabel());
    }
}
