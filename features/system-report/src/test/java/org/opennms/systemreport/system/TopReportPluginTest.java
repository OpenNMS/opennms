/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.ResourceLocator;
import org.springframework.core.io.FileSystemResource;

public class TopReportPluginTest {
    private ResourceLocator m_resourceLocator;
    private TopReportPlugin m_reportPlugin;

    public TopReportPluginTest() {
        final Properties props = new Properties();
        props.put("log4j.logger.org.opennms.systemreport.system", "DEBUG");
        MockLogAppender.setupLogging(true, "DEBUG", props);
    }

    @Before
    public void setUp() throws Exception {
        m_resourceLocator = Mockito.mock(ResourceLocator.class);
        m_reportPlugin = new TopReportPlugin();

        final Method m = m_reportPlugin.getClass().getSuperclass().getDeclaredMethod("setResourceLocator", ResourceLocator.class);
        m.setAccessible(true);
        m_reportPlugin.getClass().getSuperclass().getDeclaredField("m_resourceLocator").setAccessible(true);
        m.invoke(m_reportPlugin, m_resourceLocator);
    }

    @Test
    public void testMissingTop() {
        Mockito.when(m_resourceLocator.findBinary(Mockito.anyString())).thenReturn(null);
        assertEquals("Top", m_reportPlugin.getName());
        assertTrue(m_reportPlugin.getDescription().contains("Output of the 'top' command"));
        assertEquals(11, m_reportPlugin.getPriority());
        assertEquals(Collections.emptyMap(), m_reportPlugin.getEntries());
    }

    @Test
    public void testMacTop() {
        final String topHOutput = "/usr/bin/top usage: /usr/bin/top\n" + 
                "                [-a | -d | -e | -c <mode>]\n" + 
                "                [-F | -f]\n" + 
                "                [-h]\n" + 
                "                [-i <interval>]\n" + 
                "                [-l <samples>]\n" + 
                "                [-ncols <columns>]\n" + 
                "                [-o <key>] [-O <secondaryKey>]\n" + 
                "                [-R | -r]\n" + 
                "                [-S]\n" + 
                "                [-s <delay>]\n" + 
                "                [-n <nprocs>]\n" + 
                "                [-stats <key(s)>]\n" + 
                "                [-pid <processid>]\n" + 
                "                [-user <username>]\n" + 
                "                [-U <username>]\n" + 
                "                [-u]\n" + 
                "\n";

        final String topOutput = "Processes: 257 total, 2 running, 5 stuck, 250 sleeping, 1423 threads \n" + 
                "2014/09/09 11:22:40\n" + 
                "Load Avg: 1.89, 1.78, 1.78 \n" + 
                "CPU usage: 6.19% user, 12.38% sys, 81.41% idle \n" + 
                "SharedLibs: 11M resident, 7176K data, 0B linkedit.\n" + 
                "MemRegions: 82604 total, 6142M resident, 129M private, 1243M shared.\n" + 
                "PhysMem: 13G used (1795M wired), 1996M unused.\n" + 
                "VM: 657G vsize, 1066M framework vsize, 0(0) swapins, 0(0) swapouts.\n" + 
                "Networks: packets: 42430011/29G in, 50045953/38G out.\n" + 
                "Disks: 2822463/53G read, 3289984/149G written.\n" + 
                "\n" + 
                "PID    COMMAND          %CPU TIME     #TH   #WQ #PORTS #MREGS MEM    RPRVT  PURG   CMPRS  VPRVT  VSIZE  PGRP  PPID  STATE    UID FAULTS    COW      MSGSENT    MSGRECV    SYSBSD     SYSMACH    CSW        PAGEINS  KPRVT  KSHRD  IDLEW    POWER USER           \n" + 
                "99811  Google Chrome He 0.0  00:03.15 10    0   98+    495+   71M+   71M+   2448K+ 0B     242M+  3373M+ 539   539   sleeping 501 55392+    1527+    5872+      2961+      36003+     310748+    10365+     3800+    1536K+ 32K+   402      0.0   ranger         \n" + 
                "99187  Google Chrome He 0.0  00:10.22 10    0   98+    598+   146M+  145M+  72K+   0B     322M+  3427M+ 539   539   sleeping 501 127168+   1523+    11244+     6630+      84945+     369104+    25889+     8198+    2166K+ 73K+   2317     0.0   ranger         \n" + 
                "99150  Google Chrome He 0.0  00:01.91 10    0   98+    452+   64M+   62M+   376K+  0B     212M+  3338M+ 539   539   sleeping 501 39301+    1522+    3340+      1408+      26671+     288536+    7425+      1821+    1410K+ -6K+   294      0.0   ranger         \n";
        Mockito.when(m_resourceLocator.findBinary("top")).thenReturn("/usr/bin/top");
        Mockito.when(m_resourceLocator.slurpOutput("/usr/bin/top -h", true)).thenReturn(topHOutput);
        Mockito.when(m_resourceLocator.slurpOutput("/usr/bin/top -l 1", false)).thenReturn(topOutput);
        assertEquals(1, m_reportPlugin.getEntries().size());
        assertEquals("Output", m_reportPlugin.getEntries().keySet().iterator().next());
        assertEquals(FileSystemResource.class, m_reportPlugin.getEntries().entrySet().iterator().next().getValue().getClass());
    }

    @Test
    public void testDebianTop() {
        final String topHOutput = "     top: procps version 3.2.7\n" + 
                "usage:  top -hv | -bcisSH -d delay -n iterations [-u user | -U user] -p pid [,pid ...]\n" + 
                "\n";

        final String topOutput = "top - 11:26:50 up 60 days, 12:21,  4 users,  load average: 0.11, 0.04, 0.01\n" + 
                "Tasks: 173 total,   3 running, 170 sleeping,   0 stopped,   0 zombie\n" + 
                "Cpu(s):  6.3%us,  0.9%sy,  0.1%ni, 91.7%id,  0.9%wa,  0.0%hi,  0.0%si,  0.0%st\n" + 
                "Mem:    775972k total,   767264k used,     8708k free,   151448k buffers\n" + 
                "Swap:  2289212k total,     4740k used,  2284472k free,   106912k cached\n" + 
                "\n" + 
                "  PID USER      PR  NI  VIRT  RES  SHR S %CPU %MEM    TIME+  COMMAND                                                                                                                                      \n" + 
                "    1 root      20   0  2100  688  588 S  0.0  0.1   1:34.46 init                                                                                                                                         \n" + 
                "    2 root      15  -5     0    0    0 S  0.0  0.0   0:00.00 kthreadd                                                                                                                                     \n" + 
                "    3 root      RT  -5     0    0    0 S  0.0  0.0   0:00.00 migration/0                                                                                                                                  \n" + 
                "    4 root      15  -5     0    0    0 S  0.0  0.0   3:24.96 ksoftirqd/0                                                                                                                                  \n" + 
                "    5 root      RT  -5     0    0    0 S  0.0  0.0   0:10.02 watchdog/0                                                                                                                                   \n" + 
                "    6 root      15  -5     0    0    0 S  0.0  0.0   8:19.05 events/0                                                                                                                                     \n";

        Mockito.when(m_resourceLocator.findBinary("top")).thenReturn("/usr/bin/top");
        Mockito.when(m_resourceLocator.slurpOutput("/usr/bin/top -h", true)).thenReturn(topHOutput);
        Mockito.when(m_resourceLocator.slurpOutput("/usr/bin/top -n 1 -b", false)).thenReturn(topOutput);
        assertEquals(1, m_reportPlugin.getEntries().size());
        assertEquals("Output", m_reportPlugin.getEntries().keySet().iterator().next());
        assertEquals(FileSystemResource.class, m_reportPlugin.getEntries().entrySet().iterator().next().getValue().getClass());
    }

    @Test
    public void testUbuntuTop() {
        final String topHOutput = "  procps-ng version 3.3.9\n" + 
                "Usage:\n" + 
                "  top -hv | -bcHiOSs -d secs -n max -u|U user -p pid(s) -o field -w [cols]\n";

        final String topOutput = "top - 11:29:59 up 10 days, 21:35,  1 user,  load average: 0.11, 0.10, 0.07\n" + 
                "Tasks: 156 total,   1 running, 155 sleeping,   0 stopped,   0 zombie\n" + 
                "%Cpu(s):  0.8 us,  0.9 sy,  8.8 ni, 89.1 id,  0.3 wa,  0.1 hi,  0.0 si,  0.0 st\n" + 
                "KiB Mem:   6093300 total,  3234980 used,  2858320 free,        8 buffers\n" + 
                "KiB Swap:  6270972 total,    94704 used,  6176268 free.  2383044 cached Mem\n" + 
                "\n" + 
                "  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND\n" + 
                "18382 bamboo    20   0   29012   1460   1052 R  12.5  0.0   0:00.02 top\n" + 
                "    1 root      20   0   33904   2684   1352 S   0.0  0.0   0:05.51 init\n";

        Mockito.when(m_resourceLocator.findBinary("top")).thenReturn("/usr/bin/top");
        Mockito.when(m_resourceLocator.slurpOutput("/usr/bin/top -h", true)).thenReturn(topHOutput);
        Mockito.when(m_resourceLocator.slurpOutput("/usr/bin/top -n 1 -b", false)).thenReturn(topOutput);
        assertEquals(1, m_reportPlugin.getEntries().size());
        assertEquals("Output", m_reportPlugin.getEntries().keySet().iterator().next());
        assertEquals(FileSystemResource.class, m_reportPlugin.getEntries().entrySet().iterator().next().getValue().getClass());
    }
}