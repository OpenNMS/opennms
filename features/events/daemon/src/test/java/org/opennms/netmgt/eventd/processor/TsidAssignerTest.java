package org.opennms.netmgt.eventd.processor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.tsid.TsidFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class TsidAssignerTest {

    private TsidAssigner tsidAssigner;

    @Before
    public void setUp() {
        tsidAssigner = new TsidAssigner(new TsidFactory(0));
    }

    @Test
    public void shouldAssignTsidToEvent() throws Exception {
        Event event = new Event();
        event.setUei("uei.opennms.org/test");
        Log log = createLog(event);

        tsidAssigner.process(log);

        assertThat(event.getDbid()).isNotNull().isPositive();
    }

    @Test
    public void shouldAssignUniqueTsidsToMultipleEvents() throws Exception {
        Event event1 = new Event();
        event1.setUei("uei.opennms.org/test1");
        Event event2 = new Event();
        event2.setUei("uei.opennms.org/test2");
        Log log = createLog(event1, event2);

        tsidAssigner.process(log);

        assertThat(event1.getDbid()).isNotNull();
        assertThat(event2.getDbid()).isNotNull();
        assertThat(event1.getDbid()).isNotEqualTo(event2.getDbid());
    }

    @Test
    public void shouldNotOverwriteExistingDbid() throws Exception {
        Event event = new Event();
        event.setUei("uei.opennms.org/test");
        event.setDbid(42L);
        Log log = createLog(event);

        tsidAssigner.process(log);

        assertThat(event.getDbid()).isEqualTo(42L);
    }

    private Log createLog(Event... events) {
        Events eventsContainer = new Events();
        for (Event e : events) {
            eventsContainer.addEvent(e);
        }
        Log log = new Log();
        log.setEvents(eventsContainer);
        return log;
    }
}
