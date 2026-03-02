package org.opennms.netmgt.eventd.router;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;

public class EventClassifierTest {

    private final EventClassifier classifier = new EventClassifier();

    @Test
    public void shouldClassifyEventWithAlarmDataAsFault() {
        Event event = eventWithAlarmData("uei.opennms.org/nodes/nodeDown");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.FAULT);
    }

    @Test
    public void shouldClassifyInternalEventWithoutAlarmDataAsIpc() {
        Event event = eventWithoutAlarmData("uei.opennms.org/internal/reloadDaemonConfig");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.IPC);
    }

    @Test
    public void shouldClassifyInternalEventWithAlarmDataAsDual() {
        Event event = eventWithAlarmData("uei.opennms.org/internal/reloadDaemonConfigFailed");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.DUAL);
    }

    @Test
    public void shouldClassifyTrapAsFault() {
        Event event = eventWithoutAlarmData("uei.opennms.org/traps/linkDown");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.FAULT);
    }

    @Test
    public void shouldClassifyCapsdInternalAsIpc() {
        Event event = eventWithoutAlarmData("uei.opennms.org/internal/capsd/forceRescan");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.IPC);
    }

    @Test
    public void shouldClassifyDiscoveryInternalAsIpc() {
        Event event = eventWithoutAlarmData("uei.opennms.org/internal/discovery/newSuspect");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.IPC);
    }

    private Event eventWithAlarmData(String uei) {
        Event event = new Event();
        event.setUei(uei);
        AlarmData ad = new AlarmData();
        ad.setReductionKey(uei + ":1");
        ad.setAlarmType(1);
        event.setAlarmData(ad);
        return event;
    }

    private Event eventWithoutAlarmData(String uei) {
        Event event = new Event();
        event.setUei(uei);
        return event;
    }
}
