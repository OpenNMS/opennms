package org.opennms.netmgt.jasper;

import static org.junit.Assert.assertTrue;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;

import org.junit.Test;
import org.opennms.netmgt.jasper.jrobin.JRobinQueryExecutorFactory;
import org.opennms.netmgt.jasper.resource.ResourceQueryExecuterFactory;
import org.opennms.netmgt.jasper.rrdtool.RrdtoolQueryExecutorFactory;

public class OnmsQueryExecutorFactoryBundleTest {

    @Test
    public void testPickCorrectStrategy() throws JRException {
        OnmsQueryExecutorFactoryBundle executorBundle = new OnmsQueryExecutorFactoryBundle();
        JRQueryExecuterFactory factory = executorBundle.getQueryExecuterFactory("jrobin");
        
        assertTrue(JRobinQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("rrdtool");
        assertTrue(RrdtoolQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("resourceQuery");
        assertTrue(ResourceQueryExecuterFactory.class == factory.getClass());
        
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy");
        factory = executorBundle.getQueryExecuterFactory("jrobin");
        assertTrue(RrdtoolQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("resourceQuery");
        assertTrue(ResourceQueryExecuterFactory.class == factory.getClass());
        
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
        factory = executorBundle.getQueryExecuterFactory("jrobin");
        assertTrue(JRobinQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("resourceQuery");
        assertTrue(ResourceQueryExecuterFactory.class == factory.getClass());
    }

}
