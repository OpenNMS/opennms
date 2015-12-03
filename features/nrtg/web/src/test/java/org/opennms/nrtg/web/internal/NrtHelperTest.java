/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.web.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.model.PrefabGraph;

/**
 * @author Markus@OpenNMS.org
 */
public class NrtHelperTest {
    private final Map<String,String> m_emptyMap = Collections.emptyMap();
    private NrtHelper nrtHelper = new NrtHelper();

    @Test
    public void testStringProperties () {
        String rawString = "--title='Current TCP Connections' --vertical-label='Current Connections ({ifSpeed})' DEF:currEstab={rrd1}:tcpCurrEstab:AVERAGE DEF:minCurrEstab={rrd1}:tcpCurrEstab:MIN DEF:maxCurrEstab={rrd1}:tcpCurrEstab:MAX LINE2:currEstab#00ff00:'Current ' GPRINT:currEstab:AVERAGE:'Avg  \n: %8.2lf %s' GPRINT:currEstab:MIN:'Min  \n: %8.2lf %s' GPRINT:currEstab:MAX:'Max  \n: %8.2lf %s\n'";
        final Map<String,String> properties = new HashMap<String,String>();
        properties.put("ifSpeed", "monkey");

        final PrefabGraph prefabGraph = new PrefabGraph("foo", "bar", new String[]{}, rawString, new String[]{}, new String[]{}, 0, null, null, null, null, new String[]{});
        final String graphString = nrtHelper.cleanUpRrdGraphStringForWebUi(prefabGraph, m_emptyMap, properties);
        assertTrue(graphString.contains("(monkey)"));
    }

    @Test
    public void testGenerateMetricMappingCurrentTcpConnections() {
        System.out.println("metrics mapping for Tcp Connections");

        String rawString = "--title='Current TCP Connections' --vertical-label='Current Connections' DEF:currEstab={rrd1}:tcpCurrEstab:AVERAGE DEF:minCurrEstab={rrd1}:tcpCurrEstab:MIN DEF:maxCurrEstab={rrd1}:tcpCurrEstab:MAX LINE2:currEstab#00ff00:'Current ' GPRINT:currEstab:AVERAGE:'Avg  \n: %8.2lf %s' GPRINT:currEstab:MIN:'Min  \n: %8.2lf %s' GPRINT:currEstab:MAX:'Max  \n: %8.2lf %s\n'";
        String expectedResult = "\".1.3.6.1.2.1.6.9.0\": \"{rrd1}:tcpCurrEstab\"";

        Map<String, String> rrdGraphAttributesToMetricIds = new LinkedHashMap<String, String>();
        rrdGraphAttributesToMetricIds.put("tcpCurrEstab", ".1.3.6.1.2.1.6.9.0");

        String mappingResult = nrtHelper.generateJsMappingObject(rawString, rrdGraphAttributesToMetricIds);

        System.err.println("mappingResult = " + mappingResult);
        assertTrue(mappingResult.contains(".1.3.6.1.2.1.6.9.0"));
        assertTrue(mappingResult.contains("tcpCurrEstab"));
        assertFalse(mappingResult.contains("\n"));

        assertEquals(expectedResult, mappingResult);
    }

    @Test
    public void testGenerateMetricMappingSystemMemoryStats() {
        System.out.println("metrics mapping for System Memory Stats");

        String rawString = "--title=\"System Memory Stats\" --width 565 --height 200 --lower-limit 0 --base=1024 --vertical-label=\"Bytes\" DEF:memavailswap={rrd1}:memAvailSwap:AVERAGE DEF:minMemavailswap={rrd1}:memAvailSwap:MIN DEF:maxMemavailswap={rrd1}:memAvailSwap:MAX DEF:memtotalreal={rrd2}:memTotalReal:AVERAGE DEF:minMemtotalreal={rrd2}:memTotalReal:MIN DEF:maxMemtotalreal={rrd2}:memTotalReal:MAX DEF:memavailreal={rrd3}:memAvailReal:AVERAGE DEF:minMemavailreal={rrd3}:memAvailReal:MIN DEF:maxMemavailreal={rrd3}:memAvailReal:MAX DEF:membuffer={rrd4}:memBuffer:AVERAGE DEF:minMembuffer={rrd4}:memBuffer:MIN DEF:maxMembuffer={rrd4}:memBuffer:MAX DEF:memcached={rrd5}:memCached:AVERAGE DEF:minMemcached={rrd5}:memCached:MIN DEF:maxMemcached={rrd5}:memCached:MAX DEF:memshared={rrd6}:memShared:AVERAGE DEF:minMemshared={rrd6}:memShared:MIN DEF:maxMemshared={rrd6}:memShared:MAX CDEF:memavailswapBytes=memavailswap,1024,* CDEF:minMemavailswapBytes=minMemavailswap,1024,* CDEF:maxMemavailswapBytes=maxMemavailswap,1024,* CDEF:memtotalrealBytes=memtotalreal,1024,* CDEF:minMemtotalrealBytes=minMemtotalreal,1024,* CDEF:maxMemtotalrealBytes=maxMemtotalreal,1024,* CDEF:memavailrealBytes=memavailreal,1024,* CDEF:minMemavailrealBytes=minMemavailreal,1024,* CDEF:maxMemavailrealBytes=maxMemavailreal,1024,* CDEF:membufferBytes=membuffer,1024,* CDEF:minMembufferBytes=minMembuffer,1024,* CDEF:maxMembufferBytes=maxMembuffer,1024,* CDEF:memcachedBytes=memcached,1024,* CDEF:minMemcachedBytes=minMemcached,1024,* CDEF:maxMemcachedBytes=maxMemcached,1024,* CDEF:memsharedBytes=memshared,UN,0,memshared,IF,1024,* CDEF:minMemsharedBytes=minMemshared,UN,0,minMemshared,IF,1024,* CDEF:maxMemsharedBytes=maxMemshared,UN,0,maxMemshared,IF,1024,* CDEF:usedBytes=memtotalrealBytes,membufferBytes,-,memcachedBytes,-,memsharedBytes,-,memavailrealBytes,- CDEF:minUsedBytes=minMemtotalrealBytes,minMembufferBytes,-,minMemcachedBytes,-,minMemsharedBytes,-,minMemavailrealBytes,- CDEF:maxUsedBytes=maxMemtotalrealBytes,maxMembufferBytes,-,maxMemcachedBytes,-,maxMemsharedBytes,-,maxMemavailrealBytes,- AREA:usedBytes#dd4400:\"Used (Other)\" GPRINT:usedBytes:AVERAGE:\"    Avg  \\: %8.2lf %s\" GPRINT:usedBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:usedBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:membufferBytes#00ffff:\"IO Buff Ram \" GPRINT:membufferBytes:AVERAGE:\"    Avg  \\: %8.2lf %s\" GPRINT:membufferBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:membufferBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memsharedBytes#000a44:\"Shared Mem    \" GPRINT:memsharedBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memsharedBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memsharedBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memcachedBytes#00aa00:\"Filesystem Cache\" GPRINT:memcachedBytes:AVERAGE:\"Avg  \\: %8.2lf %s\" GPRINT:memcachedBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memcachedBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memavailrealBytes#00ff00:\"Avail Real Mem\" GPRINT:memavailrealBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memavailrealBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memavailrealBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memavailswapBytes#ff0000:\"Total Swap    \" GPRINT:memavailswapBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memavailswapBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memavailswapBytes:MAX:\"Max  \\: %8.2lf %s\\n\" LINE2:memtotalrealBytes#0000ff:\"Total Real Mem\" GPRINT:memtotalrealBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memtotalrealBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memtotalrealBytes:MAX:\"Max  \\: %8.2lf %s\\n\"";
        String expectedResult = "\".1.3.6.1.4.1.2021.4.4.0\": \"{rrd1}:memAvailSwap\", \n"
                + "\".1.3.6.1.4.1.2021.4.5.0\": \"{rrd2}:memTotalReal\", \n"
                + "\".1.3.6.1.4.1.2021.4.6.0\": \"{rrd3}:memAvailReal\", \n"
                + "\".1.3.6.1.4.1.2021.4.14.0\": \"{rrd4}:memBuffer\", \n"
                + "\".1.3.6.1.4.1.2021.4.15.0\": \"{rrd5}:memCached\", \n"
                + "\".1.3.6.1.4.1.2021.4.13.0\": \"{rrd6}:memShared\"";

        Map<String, String> rrdGraphAttributesToMetricIds = new LinkedHashMap<String, String>();
        rrdGraphAttributesToMetricIds.put("memAvailSwap", ".1.3.6.1.4.1.2021.4.4.0");
        rrdGraphAttributesToMetricIds.put("memTotalReal", ".1.3.6.1.4.1.2021.4.5.0");
        rrdGraphAttributesToMetricIds.put("memAvailReal", ".1.3.6.1.4.1.2021.4.6.0");
        rrdGraphAttributesToMetricIds.put("memBuffer", ".1.3.6.1.4.1.2021.4.14.0");
        rrdGraphAttributesToMetricIds.put("memCached", ".1.3.6.1.4.1.2021.4.15.0");
        rrdGraphAttributesToMetricIds.put("memShared", ".1.3.6.1.4.1.2021.4.13.0");

        String mappingResult = nrtHelper.generateJsMappingObject(rawString, rrdGraphAttributesToMetricIds);

        assertTrue(mappingResult.contains(".1.3.6.1.4.1.2021.4.4.0"));
        assertTrue(mappingResult.contains(".1.3.6.1.4.1.2021.4.5.0"));
        assertTrue(mappingResult.contains(".1.3.6.1.4.1.2021.4.6.0"));
        assertTrue(mappingResult.contains(".1.3.6.1.4.1.2021.4.14.0"));
        assertTrue(mappingResult.contains(".1.3.6.1.4.1.2021.4.15.0"));
        assertTrue(mappingResult.contains(".1.3.6.1.4.1.2021.4.13.0"));
        assertTrue(mappingResult.contains("{rrd1}:memAvailSwap"));
        assertTrue(mappingResult.contains("{rrd2}:memTotalReal"));
        assertTrue(mappingResult.contains("{rrd3}:memAvailReal"));
        assertTrue(mappingResult.contains("{rrd4}:memBuffer"));
        assertTrue(mappingResult.contains("{rrd5}:memCached"));
        assertTrue(mappingResult.contains("{rrd6}:memShared"));

        assertEquals(expectedResult, mappingResult);
    }

    @Test
    public void testGenerateMetricMappingTcpOpenConnections() {
        System.out.println("metrics mapping for TCP Open Connections");

        String rawString = "--title=\"TCP Open Connections\" --vertical-label=\"TCP Opens Per Second\" DEF:actOpen={rrd1}:tcpActiveOpens:AVERAGE DEF:minActOpen={rrd1}:tcpActiveOpens:MIN DEF:maxActOpen={rrd1}:tcpActiveOpens:MAX DEF:passOpen={rrd2}:tcpPassiveOpens:AVERAGE DEF:minPassOpen={rrd2}:tcpPassiveOpens:MIN DEF:maxPassOpen={rrd2}:tcpPassiveOpens:MAX CDEF:negActOpen=0,actOpen,- AREA:passOpen#00ff00:\"In (Passive)\" GPRINT:passOpen:AVERAGE:\"Avg  \\: %8.2lf %s\" GPRINT:passOpen:MIN:\"Min  \\: %8.2lf %s\" GPRINT:passOpen:MAX:\"Max  \\: %8.2lf %s\\n\" AREA:negActOpen#0000ff:\"Out (Active)\" GPRINT:actOpen:AVERAGE:\"Avg  \\: %8.2lf %s\" GPRINT:actOpen:MIN:\"Min  \\: %8.2lf %s\" GPRINT:actOpen:MAX:\"Max  \\: %8.2lf %s\\n\"";
        String expectedResult = "\".1.3.6.1.2.1.6.5.0\": \"{rrd1}:tcpActiveOpens\", \n\".1.3.6.1.2.1.6.6.0\": \"{rrd2}:tcpPassiveOpens\"";

        Map<String, String> rrdGraphAttributesToMetricIds = new LinkedHashMap<String, String>();
        rrdGraphAttributesToMetricIds.put("tcpActiveOpens", ".1.3.6.1.2.1.6.5.0");
        rrdGraphAttributesToMetricIds.put("tcpPassiveOpens", ".1.3.6.1.2.1.6.6.0");

        String mappingResult = nrtHelper.generateJsMappingObject(rawString, rrdGraphAttributesToMetricIds);

        assertTrue(mappingResult.contains(".1.3.6.1.2.1.6.5.0"));
        assertTrue(mappingResult.contains(".1.3.6.1.2.1.6.6.0"));
        assertTrue(mappingResult.contains("{rrd1}:tcpActiveOpens"));
        assertTrue(mappingResult.contains("{rrd2}:tcpPassiveOpens"));

        assertEquals(expectedResult, mappingResult);
    }

    @Test
    public void testGenerateMetricMappingTca() {
        System.out.println("metrics mapping for TCA");

        String rawString = "--watermark=\"NRTG Alpha 1.0\" --slope-mode --width=960 --height=400 --title=\"TCA Delay\" --vertical-label=\"Microseconds\" DEF:in={rrd1}:inboundDelay:AVERAGE DEF:out={rrd2}:outboundDelay:AVERAGE AREA:in#00ff00:\"Inbound \" GPRINT:in:AVERAGE:\" Avg \\: %8.2lf %s\" GPRINT:in:MIN:\"Min \\: %8.2lf %s\" GPRINT:in:MAX:\"Max \\: %8.2lf %s\\n\" LINE1:out#0000ff:\"Outbound\" GPRINT:out:AVERAGE:\" Avg \\: %8.2lf %s\" GPRINT:out:MIN:\"Min \\: %8.2lf %s\" GPRINT:out:MAX:\"Max \\: %8.2lf %s\\n\"";
        String expectedResult = "\".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundDelay\": \"{rrd1}:inboundDelay\", \n\".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_outboundDelay\": \"{rrd2}:outboundDelay\"";

        Map<String, String> rrdGraphAttributesToMetricIds = new LinkedHashMap<String, String>();
        rrdGraphAttributesToMetricIds.put("inboundDelay", ".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundDelay");
        rrdGraphAttributesToMetricIds.put("outboundDelay", ".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_outboundDelay");

        String mappingResult = nrtHelper.generateJsMappingObject(rawString, rrdGraphAttributesToMetricIds);

        assertTrue(mappingResult.contains(".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundDelay"));
        assertFalse(mappingResult.contains("TCA_.1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundDelay"));

        assertEquals(expectedResult, mappingResult);
    }
}