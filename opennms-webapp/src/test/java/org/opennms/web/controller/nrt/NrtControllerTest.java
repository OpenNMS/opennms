package org.opennms.web.controller.nrt;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import org.opennms.netmgt.model.PrefabGraph;

import static org.easymock.EasyMock.*;

/**
 * @author Markus@OpenNMS.org
 */
public class NrtControllerTest {

    @Test
    public void testRrdGraphPrepWithCurrentTcpConnections() {
        System.out.println("rrdGraphPrep with Tcp Connections");

        String rawString = "--title='Current TCP Connections' --vertical-label='Current Connections' DEF:currEstab={rrd1}:tcpCurrEstab:AVERAGE DEF:minCurrEstab={rrd1}:tcpCurrEstab:MIN DEF:maxCurrEstab={rrd1}:tcpCurrEstab:MAX LINE2:currEstab#00ff00:'Current ' GPRINT:currEstab:AVERAGE:'Avg  \n: %8.2lf %s' GPRINT:currEstab:MIN:'Min  \n: %8.2lf %s' GPRINT:currEstab:MAX:'Max  \n: %8.2lf %s\n'";
        String expectedResult = "--title='Current TCP Connections' --vertical-label='Current Connections' DEF:currEstab=GRAPH:tcpCurrEstab:AVERAGE DEF:minCurrEstab=GRAPH:tcpCurrEstab:MIN DEF:maxCurrEstab=GRAPH:tcpCurrEstab:MAX LINE2:currEstab#00ff00:'Current ' GPRINT:currEstab:AVERAGE:'Avg  \n: %8.2lf %s' GPRINT:currEstab:MIN:'Min  \n: %8.2lf %s' GPRINT:currEstab:MAX:'Max  \n: %8.2lf %s\n'";

        String[] columns = new String[]{"tcpCurrEstab"};
        String[] metrics = new String[]{".1.3.6.1.2.1.6.9.0"};

        PrefabGraph prefabGraph = createMock(PrefabGraph.class);
        expect(prefabGraph.getCommand()).andReturn(rawString).anyTimes();
        expect(prefabGraph.getColumns()).andReturn(columns).anyTimes();
        expect(prefabGraph.getMetricIds()).andReturn(metrics).anyTimes();
        replay(prefabGraph);

        NrtController instance = new NrtController();

        NrtUIMetricMappings nrtUIMetricMappings = instance.rrdGraphPrep(prefabGraph);

        // System.out.println(nrtUIMetricMappings.getJavaScriptObjects());

        for (String metric : metrics) {
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().containsKey(metric));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("MAX"));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("MIN"));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("AVERAGE"));
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().size(), 3);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("MAX").size(), 1);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("MIN").size(), 1);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("AVERAGE").size(), 1);
        }

        assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().size(), metrics.length);

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.9.0").getTargetsForConsolidationFunction("MAX").contains("maxCurrEstab"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.9.0").getTargetsForConsolidationFunction("MIN").contains("minCurrEstab"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.9.0").getTargetsForConsolidationFunction("AVERAGE").contains("currEstab"));

        assertEquals(expectedResult, nrtUIMetricMappings.getModifiedString());
    }

    @Test
    public void testRrdGraphPrepWithTcpOpenConnections() {
        System.out.println("rrdGraphPrep with TCP Open Connections");

        String rawString = "--title=\"TCP Open Connections\" --vertical-label=\"TCP Opens Per Second\" DEF:actOpen={rrd1}:tcpActiveOpens:AVERAGE DEF:minActOpen={rrd1}:tcpActiveOpens:MIN DEF:maxActOpen={rrd1}:tcpActiveOpens:MAX DEF:passOpen={rrd2}:tcpPassiveOpens:AVERAGE DEF:minPassOpen={rrd2}:tcpPassiveOpens:MIN DEF:maxPassOpen={rrd2}:tcpPassiveOpens:MAX CDEF:negActOpen=0,actOpen,- AREA:passOpen#00ff00:\"In (Passive)\" GPRINT:passOpen:AVERAGE:\"Avg  \\: %8.2lf %s\" GPRINT:passOpen:MIN:\"Min  \\: %8.2lf %s\" GPRINT:passOpen:MAX:\"Max  \\: %8.2lf %s\\n\" AREA:negActOpen#0000ff:\"Out (Active)\" GPRINT:actOpen:AVERAGE:\"Avg  \\: %8.2lf %s\" GPRINT:actOpen:MIN:\"Min  \\: %8.2lf %s\" GPRINT:actOpen:MAX:\"Max  \\: %8.2lf %s\\n\"";
        String expectedResult = "--title='TCP Open Connections' --vertical-label='TCP Opens Per Second' DEF:actOpen=GRAPH:tcpActiveOpens:AVERAGE DEF:minActOpen=GRAPH:tcpActiveOpens:MIN DEF:maxActOpen=GRAPH:tcpActiveOpens:MAX DEF:passOpen=GRAPH:tcpPassiveOpens:AVERAGE DEF:minPassOpen=GRAPH:tcpPassiveOpens:MIN DEF:maxPassOpen=GRAPH:tcpPassiveOpens:MAX CDEF:negActOpen=0,actOpen,- AREA:passOpen#00ff00:'In (Passive)' GPRINT:passOpen:AVERAGE:'Avg  \\: %8.2lf %s' GPRINT:passOpen:MIN:'Min  \\: %8.2lf %s' GPRINT:passOpen:MAX:'Max  \\: %8.2lf %s\\n' AREA:negActOpen#0000ff:'Out (Active)' GPRINT:actOpen:AVERAGE:'Avg  \\: %8.2lf %s' GPRINT:actOpen:MIN:'Min  \\: %8.2lf %s' GPRINT:actOpen:MAX:'Max  \\: %8.2lf %s\\n'";
        String[] columns = new String[]{"tcpActiveOpens", "tcpPassiveOpens"};
        String[] metrics = new String[]{".1.3.6.1.2.1.6.5.0", ".1.3.6.1.2.1.6.6.0"};

        PrefabGraph prefabGraph = createMock(PrefabGraph.class);
        expect(prefabGraph.getCommand()).andReturn(rawString).anyTimes();
        expect(prefabGraph.getColumns()).andReturn(columns).anyTimes();
        expect(prefabGraph.getMetricIds()).andReturn(metrics).anyTimes();
        replay(prefabGraph);

        NrtController instance = new NrtController();

        NrtUIMetricMappings nrtUIMetricMappings = instance.rrdGraphPrep(prefabGraph);

        // System.out.println(nrtUIMetricMappings.getJavaScriptObjects());

        for (String metric : metrics) {
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().containsKey(metric));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("MAX"));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("MIN"));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("AVERAGE"));
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().size(), 3);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("MAX").size(), 1);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("MIN").size(), 1);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("AVERAGE").size(), 1);
        }

        assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().size(), metrics.length);

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.5.0").getTargetsForConsolidationFunction("MAX").contains("maxActOpen"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.5.0").getTargetsForConsolidationFunction("MIN").contains("minActOpen"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.5.0").getTargetsForConsolidationFunction("AVERAGE").contains("actOpen"));

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.6.0").getTargetsForConsolidationFunction("MAX").contains("maxPassOpen"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.6.0").getTargetsForConsolidationFunction("MIN").contains("minPassOpen"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.2.1.6.6.0").getTargetsForConsolidationFunction("AVERAGE").contains("passOpen"));

        assertEquals(nrtUIMetricMappings.getModifiedString(), expectedResult);
    }

    @Test
    public void testRrdGraphPrepWithSystemMemoryStats() {
        System.out.println("rrdGraphPrep with System Memory Stats");

        String rawString = "--title=\"System Memory Stats\" --width 565 --height 200 --lower-limit 0 --base=1024 --vertical-label=\"Bytes\" DEF:memavailswap={rrd1}:memAvailSwap:AVERAGE DEF:minMemavailswap={rrd1}:memAvailSwap:MIN DEF:maxMemavailswap={rrd1}:memAvailSwap:MAX DEF:memtotalreal={rrd2}:memTotalReal:AVERAGE DEF:minMemtotalreal={rrd2}:memTotalReal:MIN DEF:maxMemtotalreal={rrd2}:memTotalReal:MAX DEF:memavailreal={rrd3}:memAvailReal:AVERAGE DEF:minMemavailreal={rrd3}:memAvailReal:MIN DEF:maxMemavailreal={rrd3}:memAvailReal:MAX DEF:membuffer={rrd4}:memBuffer:AVERAGE DEF:minMembuffer={rrd4}:memBuffer:MIN DEF:maxMembuffer={rrd4}:memBuffer:MAX DEF:memcached={rrd5}:memCached:AVERAGE DEF:minMemcached={rrd5}:memCached:MIN DEF:maxMemcached={rrd5}:memCached:MAX DEF:memshared={rrd6}:memShared:AVERAGE DEF:minMemshared={rrd6}:memShared:MIN DEF:maxMemshared={rrd6}:memShared:MAX CDEF:memavailswapBytes=memavailswap,1024,* CDEF:minMemavailswapBytes=minMemavailswap,1024,* CDEF:maxMemavailswapBytes=maxMemavailswap,1024,* CDEF:memtotalrealBytes=memtotalreal,1024,* CDEF:minMemtotalrealBytes=minMemtotalreal,1024,* CDEF:maxMemtotalrealBytes=maxMemtotalreal,1024,* CDEF:memavailrealBytes=memavailreal,1024,* CDEF:minMemavailrealBytes=minMemavailreal,1024,* CDEF:maxMemavailrealBytes=maxMemavailreal,1024,* CDEF:membufferBytes=membuffer,1024,* CDEF:minMembufferBytes=minMembuffer,1024,* CDEF:maxMembufferBytes=maxMembuffer,1024,* CDEF:memcachedBytes=memcached,1024,* CDEF:minMemcachedBytes=minMemcached,1024,* CDEF:maxMemcachedBytes=maxMemcached,1024,* CDEF:memsharedBytes=memshared,UN,0,memshared,IF,1024,* CDEF:minMemsharedBytes=minMemshared,UN,0,minMemshared,IF,1024,* CDEF:maxMemsharedBytes=maxMemshared,UN,0,maxMemshared,IF,1024,* CDEF:usedBytes=memtotalrealBytes,membufferBytes,-,memcachedBytes,-,memsharedBytes,-,memavailrealBytes,- CDEF:minUsedBytes=minMemtotalrealBytes,minMembufferBytes,-,minMemcachedBytes,-,minMemsharedBytes,-,minMemavailrealBytes,- CDEF:maxUsedBytes=maxMemtotalrealBytes,maxMembufferBytes,-,maxMemcachedBytes,-,maxMemsharedBytes,-,maxMemavailrealBytes,- AREA:usedBytes#dd4400:\"Used (Other)\" GPRINT:usedBytes:AVERAGE:\"    Avg  \\: %8.2lf %s\" GPRINT:usedBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:usedBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:membufferBytes#00ffff:\"IO Buff Ram \" GPRINT:membufferBytes:AVERAGE:\"    Avg  \\: %8.2lf %s\" GPRINT:membufferBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:membufferBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memsharedBytes#000a44:\"Shared Mem    \" GPRINT:memsharedBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memsharedBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memsharedBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memcachedBytes#00aa00:\"Filesystem Cache\" GPRINT:memcachedBytes:AVERAGE:\"Avg  \\: %8.2lf %s\" GPRINT:memcachedBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memcachedBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memavailrealBytes#00ff00:\"Avail Real Mem\" GPRINT:memavailrealBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memavailrealBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memavailrealBytes:MAX:\"Max  \\: %8.2lf %s\\n\" STACK:memavailswapBytes#ff0000:\"Total Swap    \" GPRINT:memavailswapBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memavailswapBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memavailswapBytes:MAX:\"Max  \\: %8.2lf %s\\n\" LINE2:memtotalrealBytes#0000ff:\"Total Real Mem\" GPRINT:memtotalrealBytes:AVERAGE:\"  Avg  \\: %8.2lf %s\" GPRINT:memtotalrealBytes:MIN:\"Min  \\: %8.2lf %s\" GPRINT:memtotalrealBytes:MAX:\"Max  \\: %8.2lf %s\\n\"";
        String expectedResult = "--title='System Memory Stats' --width 565 --height 200 --lower-limit 0 --base=1024 --vertical-label='Bytes' DEF:memavailswap=GRAPH:memAvailSwap:AVERAGE DEF:minMemavailswap=GRAPH:memAvailSwap:MIN DEF:maxMemavailswap=GRAPH:memAvailSwap:MAX DEF:memtotalreal=GRAPH:memTotalReal:AVERAGE DEF:minMemtotalreal=GRAPH:memTotalReal:MIN DEF:maxMemtotalreal=GRAPH:memTotalReal:MAX DEF:memavailreal=GRAPH:memAvailReal:AVERAGE DEF:minMemavailreal=GRAPH:memAvailReal:MIN DEF:maxMemavailreal=GRAPH:memAvailReal:MAX DEF:membuffer=GRAPH:memBuffer:AVERAGE DEF:minMembuffer=GRAPH:memBuffer:MIN DEF:maxMembuffer=GRAPH:memBuffer:MAX DEF:memcached=GRAPH:memCached:AVERAGE DEF:minMemcached=GRAPH:memCached:MIN DEF:maxMemcached=GRAPH:memCached:MAX DEF:memshared=GRAPH:memShared:AVERAGE DEF:minMemshared=GRAPH:memShared:MIN DEF:maxMemshared=GRAPH:memShared:MAX CDEF:memavailswapBytes=memavailswap,1024,* CDEF:minMemavailswapBytes=minMemavailswap,1024,* CDEF:maxMemavailswapBytes=maxMemavailswap,1024,* CDEF:memtotalrealBytes=memtotalreal,1024,* CDEF:minMemtotalrealBytes=minMemtotalreal,1024,* CDEF:maxMemtotalrealBytes=maxMemtotalreal,1024,* CDEF:memavailrealBytes=memavailreal,1024,* CDEF:minMemavailrealBytes=minMemavailreal,1024,* CDEF:maxMemavailrealBytes=maxMemavailreal,1024,* CDEF:membufferBytes=membuffer,1024,* CDEF:minMembufferBytes=minMembuffer,1024,* CDEF:maxMembufferBytes=maxMembuffer,1024,* CDEF:memcachedBytes=memcached,1024,* CDEF:minMemcachedBytes=minMemcached,1024,* CDEF:maxMemcachedBytes=maxMemcached,1024,* CDEF:memsharedBytes=memshared,UN,0,memshared,IF,1024,* CDEF:minMemsharedBytes=minMemshared,UN,0,minMemshared,IF,1024,* CDEF:maxMemsharedBytes=maxMemshared,UN,0,maxMemshared,IF,1024,* CDEF:usedBytes=memtotalrealBytes,membufferBytes,-,memcachedBytes,-,memsharedBytes,-,memavailrealBytes,- CDEF:minUsedBytes=minMemtotalrealBytes,minMembufferBytes,-,minMemcachedBytes,-,minMemsharedBytes,-,minMemavailrealBytes,- CDEF:maxUsedBytes=maxMemtotalrealBytes,maxMembufferBytes,-,maxMemcachedBytes,-,maxMemsharedBytes,-,maxMemavailrealBytes,- AREA:usedBytes#dd4400:'Used (Other)' GPRINT:usedBytes:AVERAGE:'    Avg  \\: %8.2lf %s' GPRINT:usedBytes:MIN:'Min  \\: %8.2lf %s' GPRINT:usedBytes:MAX:'Max  \\: %8.2lf %s\\n' STACK:membufferBytes#00ffff:'IO Buff Ram ' GPRINT:membufferBytes:AVERAGE:'    Avg  \\: %8.2lf %s' GPRINT:membufferBytes:MIN:'Min  \\: %8.2lf %s' GPRINT:membufferBytes:MAX:'Max  \\: %8.2lf %s\\n' STACK:memsharedBytes#000a44:'Shared Mem    ' GPRINT:memsharedBytes:AVERAGE:'  Avg  \\: %8.2lf %s' GPRINT:memsharedBytes:MIN:'Min  \\: %8.2lf %s' GPRINT:memsharedBytes:MAX:'Max  \\: %8.2lf %s\\n' STACK:memcachedBytes#00aa00:'Filesystem Cache' GPRINT:memcachedBytes:AVERAGE:'Avg  \\: %8.2lf %s' GPRINT:memcachedBytes:MIN:'Min  \\: %8.2lf %s' GPRINT:memcachedBytes:MAX:'Max  \\: %8.2lf %s\\n' STACK:memavailrealBytes#00ff00:'Avail Real Mem' GPRINT:memavailrealBytes:AVERAGE:'  Avg  \\: %8.2lf %s' GPRINT:memavailrealBytes:MIN:'Min  \\: %8.2lf %s' GPRINT:memavailrealBytes:MAX:'Max  \\: %8.2lf %s\\n' STACK:memavailswapBytes#ff0000:'Total Swap    ' GPRINT:memavailswapBytes:AVERAGE:'  Avg  \\: %8.2lf %s' GPRINT:memavailswapBytes:MIN:'Min  \\: %8.2lf %s' GPRINT:memavailswapBytes:MAX:'Max  \\: %8.2lf %s\\n' LINE2:memtotalrealBytes#0000ff:'Total Real Mem' GPRINT:memtotalrealBytes:AVERAGE:'  Avg  \\: %8.2lf %s' GPRINT:memtotalrealBytes:MIN:'Min  \\: %8.2lf %s' GPRINT:memtotalrealBytes:MAX:'Max  \\: %8.2lf %s\\n'";

        ;
        String[] columns = new String[]{"memAvailSwap", "memTotalReal", "memAvailReal", "memBuffer", "memCached", "memShared"};
        String[] metrics = new String[]{".1.3.6.1.4.1.2021.4.4.0", ".1.3.6.1.4.1.2021.4.5.0", ".1.3.6.1.4.1.2021.4.6.0", ".1.3.6.1.4.1.2021.4.14.0", ".1.3.6.1.4.1.2021.4.15.0", ".1.3.6.1.4.1.2021.4.13.0"};

        PrefabGraph prefabGraph = createMock(PrefabGraph.class);
        expect(prefabGraph.getCommand()).andReturn(rawString).anyTimes();
        expect(prefabGraph.getColumns()).andReturn(columns).anyTimes();
        expect(prefabGraph.getMetricIds()).andReturn(metrics).anyTimes();
        replay(prefabGraph);

        NrtController instance = new NrtController();
        NrtUIMetricMappings nrtUIMetricMappings = instance.rrdGraphPrep(prefabGraph);

        // System.out.println(nrtUIMetricMappings.getJavaScriptObjects());

        for (String metric : metrics) {
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().containsKey(metric));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("MAX"));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("MIN"));
            assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().contains("AVERAGE"));
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getConsolidationFunctions().size(), 3);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("MAX").size(), 1);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("MIN").size(), 1);
            assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().get(metric).getTargetsForConsolidationFunction("AVERAGE").size(), 1);
        }

        assertEquals(nrtUIMetricMappings.getNrtUIObjectHelpers().size(), metrics.length);

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.4.0").getTargetsForConsolidationFunction("MAX").contains("maxMemavailswap"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.4.0").getTargetsForConsolidationFunction("MIN").contains("minMemavailswap"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.4.0").getTargetsForConsolidationFunction("AVERAGE").contains("memavailswap"));

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.5.0").getTargetsForConsolidationFunction("MAX").contains("maxMemtotalreal"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.5.0").getTargetsForConsolidationFunction("MIN").contains("minMemtotalreal"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.5.0").getTargetsForConsolidationFunction("AVERAGE").contains("memtotalreal"));

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.6.0").getTargetsForConsolidationFunction("MAX").contains("maxMemavailreal"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.6.0").getTargetsForConsolidationFunction("MIN").contains("minMemavailreal"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.6.0").getTargetsForConsolidationFunction("AVERAGE").contains("memavailreal"));

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.14.0").getTargetsForConsolidationFunction("MAX").contains("maxMembuffer"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.14.0").getTargetsForConsolidationFunction("MIN").contains("minMembuffer"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.14.0").getTargetsForConsolidationFunction("AVERAGE").contains("membuffer"));

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.15.0").getTargetsForConsolidationFunction("MAX").contains("maxMemcached"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.15.0").getTargetsForConsolidationFunction("MIN").contains("minMemcached"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.15.0").getTargetsForConsolidationFunction("AVERAGE").contains("memcached"));

        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.13.0").getTargetsForConsolidationFunction("MAX").contains("maxMemshared"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.13.0").getTargetsForConsolidationFunction("MIN").contains("minMemshared"));
        assertTrue(nrtUIMetricMappings.getNrtUIObjectHelpers().get(".1.3.6.1.4.1.2021.4.13.0").getTargetsForConsolidationFunction("AVERAGE").contains("memshared"));

        assertEquals(nrtUIMetricMappings.getModifiedString(), expectedResult);
    }
}
