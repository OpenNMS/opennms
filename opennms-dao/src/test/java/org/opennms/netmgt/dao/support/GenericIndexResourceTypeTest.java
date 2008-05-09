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
// 2008 May 09: Created this file. - dj@opennms.org
//
// Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.dao.support;

import static org.easymock.EasyMock.expect;

import java.io.File;

import junit.framework.TestCase;

import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
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
        
        RrdTestUtils.initializeNullStrategy();

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

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexNumber() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:3}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "4", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexBogus() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:foo}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${index:foo}", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexBadNumber() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:4}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${index:4}", resource.getLabel());
    }
    
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexBeginning() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:1-}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "2.3.4", resource.getLabel());
    }
    
    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexEnding() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:-2}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "1.2.3", resource.getLabel());
    }
    

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexEmptyRange() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:-}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${index:-}", resource.getLabel());
    }
    

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexStartOutOfBounds() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:4-}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${index:4-}", resource.getLabel());
    }
    

    public void testGetResourceByNodeAndIndexGetLabelIndexWithSubIndexEndOutOfBounds() {
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "${index:-4}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "1.2.3.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "${index:-4}", resource.getLabel());
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
        GenericIndexResourceType rt = new GenericIndexResourceType(m_resourceDao, "foo", "Foo Resource", "MAC Address ${hex(index:0-5)} on interface ${index:6}", m_storageStrategy);
        
        expect(m_resourceDao.getRrdDirectory()).andReturn(new File("/a/bogus/directory"));
        
        m_mocks.replayAll();
        OnmsResource resource = rt.getResourceByNodeAndIndex(1, "0.21.109.80.9.66.4");
        m_mocks.verifyAll();
        
        assertNotNull("resource", resource);
        assertEquals("resource label", "MAC Address 00:15:6D:50:09:42 on interface 4", resource.getLabel());
    }
    
    
}
