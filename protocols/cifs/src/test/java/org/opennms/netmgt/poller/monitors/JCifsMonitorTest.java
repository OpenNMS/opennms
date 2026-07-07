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
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import jcifs.CIFSContext;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;

public class JCifsMonitorTest {
    private SmbFile mockSmbFileValidPath;
    private SmbFile mockSmbFileInvalidPath;
    private SmbFile mockSmbFolderNotEmpty;
    private SmbFile mockSmbFolderEmpty;
    private SmbFile mockSmbFileSmbException;
    private SmbFile mockSmbFileSmbHost;

    /**
     * JCifsMonitor subclass whose SmbFile factory hands out the mocks above instead of
     * opening real CIFS connections.
     */
    private JCifsMonitor jCifsMonitor;

    @Before
    public void setUp() throws Exception {
        jcifs.Config.registerSmbURLHandler();

        mockSmbFileValidPath = mock(SmbFile.class);
        when(mockSmbFileValidPath.exists()).thenReturn(true);

        mockSmbFileInvalidPath = mock(SmbFile.class);
        when(mockSmbFileInvalidPath.exists()).thenReturn(false);

        mockSmbFolderEmpty = mock(SmbFile.class);
        when(mockSmbFolderEmpty.exists()).thenReturn(true);
        when(mockSmbFolderEmpty.list(any(SmbFilenameFilter.class))).thenReturn(new String[]{});

        mockSmbFolderNotEmpty = mock(SmbFile.class);
        when(mockSmbFolderNotEmpty.exists()).thenReturn(true);
        when(mockSmbFolderNotEmpty.list(any(SmbFilenameFilter.class))).thenReturn(new String[]{"ABCD", "ACBD", "DCBA", "DABC"});

        mockSmbFileSmbException = mock(SmbFile.class);
        when(mockSmbFileSmbException.exists()).thenThrow(new SmbException(SmbException.ERROR_ACCESS_DENIED, true));

        mockSmbFileSmbHost = mock(SmbFile.class);
        when(mockSmbFileSmbHost.exists()).thenThrow(new SmbException(SmbException.ERROR_ACCESS_DENIED, true));

        jCifsMonitor = new JCifsMonitor() {
            @Override
            protected SmbFile createSmbFile(final String url, final CIFSContext context) throws MalformedURLException {
                switch (url) {
                    case "smb://10.123.123.123/validPath":
                        return mockSmbFileValidPath;
                    case "smb://10.123.123.123/invalidPath":
                        return mockSmbFileInvalidPath;
                    case "smb://10.123.123.123/folderEmpty":
                        return mockSmbFolderEmpty;
                    case "smb://10.123.123.123/folderNotEmpty":
                        return mockSmbFolderNotEmpty;
                    case "smb://10.123.123.123/smbException":
                        return mockSmbFileSmbException;
                    case "smb://10.123.123.123/malformedUrlException":
                        throw new MalformedURLException("nah, you blew it buddy");
                    case "smb://192.168.0.123/smbException":
                        return mockSmbFileSmbHost;
                    default:
                        throw new IllegalStateException("unexpected SMB url " + url);
                }
            }
        };
    }

    @Test
    public void testPoll() throws UnknownHostException {

        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddress.getByName("10.123.123.123"), "JCIFS");

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());

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
        assertEquals(PollStatus.down(""), pollStatus);

        /*
         * checking path does exist and mode is PATH_NOT_EXIST => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_NOT_EXIST");
        m.put("path", "/validPath");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(""), pollStatus);

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
        assertEquals(PollStatus.down(""), pollStatus);

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
        assertEquals(PollStatus.down(""), pollStatus);

        /*
         * checking for invalid mode => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "ABC");
        m.put("path", "/folderEmpty");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.unknown(""), pollStatus);

        /*
         * checking for SmbException => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/smbException");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(""), pollStatus);

        /*
         * checking for MalformedUrlException => down
         */
        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/malformedUrlException");

        pollStatus = jCifsMonitor.poll(svc, m);
        assertEquals(PollStatus.down(""), pollStatus);
        assertTrue("'" + pollStatus.getReason() + "' should contain 'you blew it buddy'", pollStatus.getReason().matches(".*you blew it buddy.*"));

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
        System.err.println("reason=" + pollStatus.getReason());
        assertTrue("'" + pollStatus.getReason() + "' should match '192.168.0.123'", pollStatus.getReason().matches(".*192\\.168\\.0\\.123.*"));
    }

    @Test
    public void testParamSub() throws UnknownHostException {

        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddress.getByName("10.123.123.123"), "JCIFS");

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());

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
