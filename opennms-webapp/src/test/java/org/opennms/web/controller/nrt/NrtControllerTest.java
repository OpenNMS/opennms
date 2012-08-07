package org.opennms.web.controller.nrt;

import org.junit.Test;
import static org.junit.Assert.*;
import org.opennms.netmgt.model.PrefabGraph;
import static org.easymock.EasyMock.*;
import org.junit.Ignore;

/**
 *
 * @author Markus@OpenNMS.org
 */
public class NrtControllerTest {

    @Ignore
    @Test
    public void testRrdGraphPrep() {
        System.out.println("rrdGraphPrep");
        String rawString =      "--title='Current TCP Connections' --vertical-label='Current Connections' DEF:currEstab={rrd1}:tcpCurrEstab:AVERAGE DEF:minCurrEstab={rrd1}:tcpCurrEstab:MIN DEF:maxCurrEstab={rrd1}:tcpCurrEstab:MAX LINE2:currEstab#00ff00:'Current ' GPRINT:currEstab:AVERAGE:'Avg  \n: %8.2lf %s' GPRINT:currEstab:MIN:'Min  \n: %8.2lf %s' GPRINT:currEstab:MAX:'Max  \n: %8.2lf %s\n'";
        String expectedResult = "--title='Current TCP Connections' --vertical-label='Current Connections' DEF:currEstab=.1.3.6.1.2.1.6.9.0:tcpCurrEstab:AVERAGE DEF:minCurrEstab=.1.3.6.1.2.1.6.9.0:tcpCurrEstab:MIN DEF:maxCurrEstab=.1.3.6.1.2.1.6.9.0:tcpCurrEstab:MAX LINE2:currEstab#00ff00:'Current ' GPRINT:currEstab:AVERAGE:'Avg  \n: %8.2lf %s' GPRINT:currEstab:MIN:'Min  \n: %8.2lf %s' GPRINT:currEstab:MAX:'Max  \n: %8.2lf %s\n'";
        String[] columns = new String[]{"tcpCurrEstab"};
        String[] metrics = new String[]{".1.3.6.1.2.1.6.9.0"};
        
        PrefabGraph prefabGraph = createMock(PrefabGraph.class);
        expect(prefabGraph.getCommand()).andReturn(rawString);
        expect(prefabGraph.getColumns()).andReturn(columns);
        expect(prefabGraph.getMetricIds()).andReturn(metrics);
        replay(prefabGraph);
        
        NrtController instance = new NrtController();
        String result = instance.rrdGraphPrep(prefabGraph);
        assertEquals(expectedResult, result);
    }
}
