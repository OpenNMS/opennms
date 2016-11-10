package org.opennms.netmgt.eventd.processor;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.MetricRegistry;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventExpanderPerfTest {
    
    @Autowired
    EventExpander eventExpander;

    @Test
    public void doIt() throws IOException {
        
        Event e = new EventBuilder("xx", "xx").getEvent();
        
        
        Date start = new Date();
        for (int i = 0; i < 100000; i++) {
            eventExpander.process(null, e);
        }
        Date end = new Date();
        
        System.err.println("MOO: " + (end.getTime() - start.getTime()));

        
        
        /*
        MetricRegistry metrics = new MetricRegistry();

        System.setProperty("opennms.home", "/home/jesse/git/opennms/target/opennms");
        DefaultEventConfDao ecd = new DefaultEventConfDao();
        ecd.setConfigResource(new FileSystemResource("/home/jesse/git/opennms/target/opennms/etc/eventconf.xml"));
        ecd.afterPropertiesSet();

        EventExpander ee = new EventExpander(metrics);
        ee.setEventConfDao(ecd);
        
        ee.afterPropertiesSet();
        
        
        */
    }

}
