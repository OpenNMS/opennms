/*
 * Scratch benchmark - NOT meant to be committed. Measures the eventd startup cost introduced by
 * loading the event configuration during context refresh (NMS-20289), with the full default
 * event set (all etc/examples/events files) persisted to the database first.
 */
package org.opennms.web.rest.v2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfGlobalSecurityDao;
import org.opennms.netmgt.dao.support.EventConfServiceHelper;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"

})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class EventConfStartupLoadBenchmarkIT {

    @Autowired private EventConfPersistenceService persistenceService;
    @Autowired private EventConfEventDao eventConfEventDao;
    @Autowired private EventConfGlobalSecurityDao eventConfGlobalSecurityDao;
    @Autowired private EventConfDao eventConfDao;

    @Test
    public void measureStartupLoadWithAllEvents() throws Exception {
        final File dir = new File("../opennms-base-assembly/src/main/filtered/etc/examples/events");
        assertTrue("example events dir must exist: " + dir.getAbsolutePath(), dir.isDirectory());
        final File[] files = dir.listFiles((d, n) -> n.endsWith(".xml"));
        assertTrue(files != null && files.length > 100);
        Arrays.sort(files);

        int persisted = 0;
        long events = 0;
        final long persistStart = System.currentTimeMillis();
        for (File f : files) {
            final Events parsed = JaxbUtils.unmarshal(Events.class, Files.readString(f.toPath()));
            if (parsed.getEvents().isEmpty()) {
                continue;
            }
            final String name = f.getName().replaceFirst("\\.xml$", "");
            persistenceService.persistEventConfFile(parsed, new EventConfSourceMetadataDto.Builder()
                    .filename(name).vendor(name.split("\\.")[0]).username("bench").now(new Date())
                    .description("").eventCount(parsed.getEvents().size()).build());
            persisted++;
            events += parsed.getEvents().size();
        }
        System.out.println("BENCH persisted " + persisted + " sources / " + events + " events in "
                + (System.currentTimeMillis() - persistStart) + " ms");

        // what EventConfDbBootstrap does at eventd startup: fetch + build, synchronously
        for (int run = 1; run <= 3; run++) {
            final long t0 = System.currentTimeMillis();
            EventConfServiceHelper.reloadEventsFromDB(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao);
            System.out.println("BENCH startup-load run " + run + ": " + (System.currentTimeMillis() - t0) + " ms");
        }

        // control: identical parse work, same JVM, plain strings detached from Hibernate
        final java.util.List<String> xml = eventConfEventDao.findEnabledEvents().stream()
                .map(org.opennms.netmgt.model.EventConfEvent::getXmlContent)
                .map(String::new) // detach from any entity state
                .collect(java.util.stream.Collectors.toList());
        for (int run = 1; run <= 3; run++) {
            final long t0 = System.currentTimeMillis();
            final long parsed = xml.parallelStream()
                    .map(x -> JaxbUtils.unmarshal(org.opennms.netmgt.xml.eventconf.Event.class, x))
                    .filter(java.util.Objects::nonNull)
                    .count();
            System.out.println("BENCH flat-parse control run " + run + ": " + (System.currentTimeMillis() - t0)
                    + " ms (" + parsed + " events)");
        }

        // direct validation: JaxbUtils builds an XMLReader per call via the legacy
        // org.xml.sax.helpers.XMLReaderFactory, which ignores JAXP and instead honors the
        // org.xml.sax.driver property (falling back to a per-call classpath service lookup,
        // which selects Xerces when xercesImpl is present). Pin the JDK parser explicitly.
        System.setProperty("org.xml.sax.driver", "com.sun.org.apache.xerces.internal.parsers.SAXParser");
        try {
            for (int run = 1; run <= 3; run++) {
                final long t0 = System.currentTimeMillis();
                final long parsed = xml.parallelStream()
                        .map(x -> JaxbUtils.unmarshal(org.opennms.netmgt.xml.eventconf.Event.class, x))
                        .filter(java.util.Objects::nonNull)
                        .count();
                System.out.println("BENCH flat-parse sax.driver=JDK run " + run + ": " + (System.currentTimeMillis() - t0)
                        + " ms (" + parsed + " events)");
            }
        } finally {
            System.clearProperty("org.xml.sax.driver");
        }
    }
}
