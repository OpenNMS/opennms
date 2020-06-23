/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replay;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * WARNING: Powermock has a bug that prevents this test from running properly on 
 * JDK version 1.7u65 or higher. We need to either update Powermock when the bug
 * is fixed or see if the regression in the JDK is fixed so that the test runs.
 * 
 * @see https://code.google.com/p/powermock/issues/detail?id=504
 * @see http://hg.openjdk.java.net/jdk9/hs-rt/hotspot/rev/4986ca806899
 * @see http://www.takipiblog.com/oracles-latest-java-8-update-broke-your-tools-how-did-it-happen/
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SmbFile.class, JCifsMonitor.class})
public class JCifsMonitorTest {
    private SmbFile mockSmbFileValidPath;
    private SmbFile mockSmbFileInvalidPath;
    private SmbFile mockSmbFolderNotEmpty;
    private SmbFile mockSmbFolderEmpty;
    private SmbFile mockSmbFileSmbException;
    private SmbFile mockSmbFileMalformedUrlException;
    private SmbFile mockSmbFileSmbHost;

    @Before
    public void setUp() throws Exception {
        mockSmbFileValidPath = createNiceMock(SmbFile.class);
        expect(mockSmbFileValidPath.exists()).andReturn(true).anyTimes();
        expectNew(SmbFile.class, new Class<?>[]{String.class, NtlmPasswordAuthentication.class}, eq("smb://10.123.123.123/validPath"), isA(NtlmPasswordAuthentication.class)).andReturn(mockSmbFileValidPath).anyTimes();

        mockSmbFileInvalidPath = createNiceMock(SmbFile.class);
        expect(mockSmbFileInvalidPath.exists()).andReturn(false).anyTimes();
        expectNew(SmbFile.class, new Class<?>[]{String.class, NtlmPasswordAuthentication.class}, eq("smb://10.123.123.123/invalidPath"), isA(NtlmPasswordAuthentication.class)).andReturn(mockSmbFileInvalidPath).anyTimes();

        mockSmbFolderEmpty = createNiceMock(SmbFile.class);
        expect(mockSmbFolderEmpty.exists()).andReturn(true).anyTimes();
        expect(mockSmbFolderEmpty.list((SmbFilenameFilter) anyObject())).andReturn(new String[]{}).anyTimes();
        expectNew(SmbFile.class, new Class<?>[]{String.class, NtlmPasswordAuthentication.class}, eq("smb://10.123.123.123/folderEmpty"), isA(NtlmPasswordAuthentication.class)).andReturn(mockSmbFolderEmpty).anyTimes();

        mockSmbFolderNotEmpty = createNiceMock(SmbFile.class);
        expect(mockSmbFolderNotEmpty.exists()).andReturn(true).anyTimes();
        expect(mockSmbFolderNotEmpty.list((SmbFilenameFilter) anyObject())).andReturn(new String[]{"ABCD", "ACBD", "DCBA", "DABC"}).anyTimes();
        expectNew(SmbFile.class, new Class<?>[]{String.class, NtlmPasswordAuthentication.class}, eq("smb://10.123.123.123/folderNotEmpty"), isA(NtlmPasswordAuthentication.class)).andReturn(mockSmbFolderNotEmpty).anyTimes();

        mockSmbFileSmbException = createNiceMock(SmbFile.class);
        expect(mockSmbFileSmbException.exists()).andThrow(new SmbException(SmbException.ERROR_ACCESS_DENIED, true));
        expectNew(SmbFile.class, new Class<?>[]{String.class, NtlmPasswordAuthentication.class}, eq("smb://10.123.123.123/smbException"), isA(NtlmPasswordAuthentication.class)).andReturn(mockSmbFileSmbException).anyTimes();

        mockSmbFileMalformedUrlException = createNiceMock(SmbFile.class);
        expect(mockSmbFileMalformedUrlException.exists()).andThrow(new SmbException(SmbException.ERROR_ACCESS_DENIED, true));
        expectNew(SmbFile.class, new Class<?>[]{String.class, NtlmPasswordAuthentication.class}, eq("smb://10.123.123.123/malformedUrlException"), isA(NtlmPasswordAuthentication.class)).andReturn(mockSmbFileMalformedUrlException).anyTimes();

        mockSmbFileSmbHost = createNiceMock(SmbFile.class);
        expect(mockSmbFileSmbHost.exists()).andThrow(new SmbException(SmbException.ERROR_ACCESS_DENIED, true));
        expectNew(SmbFile.class, new Class<?>[]{String.class, NtlmPasswordAuthentication.class}, eq("smb://192.168.0.123/smbException"), isA(NtlmPasswordAuthentication.class)).andReturn(mockSmbFileSmbHost).anyTimes();
    }

    @Test
    public void testPoll() throws UnknownHostException {

        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddress.getByName("10.123.123.123"), "JCIFS");

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());

        replay(mockSmbFolderEmpty, mockSmbFolderNotEmpty, mockSmbFileValidPath, mockSmbFileInvalidPath, SmbFile.class);

        JCifsMonitor jCifsMonitor = new JCifsMonitor();

        PollStatus pollStatus;

        /*
         * checking path does exist and mode is PATH_EXIST => up
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/validPath");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.up(), pollStatus);

        /*
         * checking path does not exist and mode is PATH_EXIST => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/invalidPath");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(), pollStatus);

        /*
         * checking path does exist and mode is PATH_NOT_EXIST => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_NOT_EXIST");
        m.put("path", "/validPath");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(), pollStatus);

        /*
         * checking path does not exist and mode is PATH_NOT_EXIST => up
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_NOT_EXIST");
        m.put("path", "/invalidPath");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.up(), pollStatus);

        /*
         * checking folder not empty and mode is FOLDER_EMPTY => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "FOLDER_EMPTY");
        m.put("path", "/folderNotEmpty");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(), pollStatus);

        /*
         * checking folder empty and mode is FOLDER_EMPTY => up
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "FOLDER_EMPTY");
        m.put("path", "/folderEmpty");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.up(), pollStatus);

        /*
         * checking folder not empty and mode is FOLDER_NOT_EMPTY => up
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "FOLDER_NOT_EMPTY");
        m.put("path", "/folderNotEmpty");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.up(), pollStatus);

        /*
         * checking folder empty and mode is FOLDER_NOT_EMPTY => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "FOLDER_NOT_EMPTY");
        m.put("path", "/folderEmpty");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(), pollStatus);

        /*
         * checking for invalid mode => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "ABC");
        m.put("path", "/folderEmpty");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.unknown(), pollStatus);

        /*
         * checking for SmbException => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/smbException");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(), pollStatus);

        /*
         * checking for MalformedUrlException => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/malformedUrlException");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(), pollStatus);

        /*
         * checking for overriding Ip address via empty string => up
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("smbHost", "");
        m.put("path", "/validPath");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.up(), pollStatus);

        /*
         * checking for overriding Ip address via smbHost => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("smbHost", "192.168.0.123");
        m.put("path", "/smbException");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(), pollStatus);
        assertTrue(pollStatus.getReason().matches(".*192\\.168\\.0\\.123.*"));
    }

    @Test
    public void testParamSub() throws UnknownHostException {

        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddress.getByName("10.123.123.123"), "JCIFS");

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());

        JCifsMonitor jCifsMonitor = new JCifsMonitor();

        m.put("username", "{ipAddr}");
        m.put("password", "{nodeLabel}");
        m.put("domain", "{nodeId}");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/validPath");

        Map<String, Object> subbedParams = jCifsMonitor.getRuntimeAttributes(svc, m);
        assertTrue(subbedParams.get("subbed-username").equals("10.123.123.123"));
        assertTrue(subbedParams.get("subbed-password").equals("10.123.123.123"));
        assertTrue(subbedParams.get("subbed-domain").equals("99"));
    }
}
