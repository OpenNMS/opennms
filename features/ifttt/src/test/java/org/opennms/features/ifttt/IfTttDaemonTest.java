/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.ifttt;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.ifttt.config.IfTttConfig;
import org.opennms.features.ifttt.helper.DefaultVariableNameExpansion;
import org.opennms.features.ifttt.helper.VariableNameExpansion;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

@RunWith(PowerMockRunner.class)
public class IfTttDaemonTest {
    private static final Logger LOG = LoggerFactory.getLogger(IfTttDaemonTest.class);

    private class ResultEntry {
        private String oldSeverity = "null", newSeverity = "null";
        private int oldCount = 0, newCount = 0;
        private String event;

        public ResultEntry(final String event, final VariableNameExpansion variableNameExpansion) {
            this.event = event;
            if (variableNameExpansion instanceof DefaultVariableNameExpansion) {
                this.oldCount = Integer.valueOf(variableNameExpansion.replace("%oc%"));
                this.newCount = Integer.valueOf(variableNameExpansion.replace("%nc%"));
                this.oldSeverity = variableNameExpansion.replace("%os%");
                this.newSeverity = variableNameExpansion.replace("%ns%");
            }
        }

        public ResultEntry(final String event, final String oldSeverity, final Integer oldCount, final String newSeverity, final Integer newCount) {
            this.event = event;
            this.oldSeverity = oldSeverity;
            this.newSeverity = newSeverity;
            this.oldCount = oldCount;
            this.newCount = newCount;
        }

        @Override
        public String toString() {
            return String.format("%s : %s/%d -> %s/%d", event, oldSeverity, oldCount, newSeverity, newCount);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof ResultEntry)) return false;

            ResultEntry that = (ResultEntry) o;

            return Objects.equals(oldCount, that.oldCount) &&
                   Objects.equals(newCount, that.newCount) &&
                   Objects.equals(oldSeverity, that.oldSeverity) &&
                   Objects.equals(newSeverity,that.newSeverity) &&
                   Objects.equals(event, that.event);
        }

        @Override
        public int hashCode() {
            return Objects.hash(oldSeverity,newSeverity,oldCount,newCount,event);
        }
    }

    private Map<Integer, OnmsNode> nodeMap;
    private Map<Integer, OnmsAlarm> alarmMap;
    private Map<String, OnmsCategory> categoryMap;

    private void addCategory(String category) {
        categoryMap.put(category, new OnmsCategory(category));
    }

    private void addNode(Integer id, String label, String... categories) {
        final OnmsNode onmsNode = new OnmsNode();
        onmsNode.setId(id);
        onmsNode.setLabel(label);
        onmsNode.setCategories(Arrays.stream(categories).map(c -> categoryMap.get(c)).collect(Collectors.toSet()));
        nodeMap.put(id, onmsNode);
    }

    private void addAlarm(Integer id, Integer nodeId, OnmsSeverity onmsSeverity) {
        addAlarm(id, nodeId, onmsSeverity, false);
    }

    private void addAlarm(Integer id, Integer nodeId, OnmsSeverity onmsSeverity, final boolean acknowledged) {
        final OnmsAlarm onmsAlarm = new OnmsAlarm(id, EventConstants.NODE_LOST_SERVICE_EVENT_UEI, null, null, onmsSeverity.getId(), new Date(), new OnmsEvent()) {
            @Override
            public boolean isAcknowledged() {
                return acknowledged;
            }
        };
        onmsAlarm.setNode(nodeMap.get(nodeId));
        alarmMap.put(id, onmsAlarm);
    }

    @Before
    public void setup() {
        MockLogAppender.setupLogging();

        categoryMap = new HashMap<>();
        addCategory("Foo");
        addCategory("Bar");
        addCategory("Xyz");

        nodeMap = new HashMap<>();
        addNode(1, "Node1", "Foo");
        addNode(2, "Node2", "Foo", "Bar");
        addNode(3, "Node3", "Bar");
        addNode(4, "Node4", "Foo", "Bar");
        addNode(5, "Node5", "Bar");
        addNode(6, "Node6", "Xyz");

        // Foo: nodes 1,2,4 -> MINOR, 5 alarms
        // Bar: nodes 2,4,5 -> MINOR, 6 alarms
        // Foo|Bar: nodes 1,2,3,4,5 -> MINOR, 8 alarms

        alarmMap = new HashMap<>();
        addAlarm(1, 1, OnmsSeverity.NORMAL);        // foo
        addAlarm(2, 2, OnmsSeverity.INDETERMINATE); // foo, bar
        addAlarm(3, 3, OnmsSeverity.MINOR);         // bar

        addAlarm(4, 4, OnmsSeverity.WARNING);       // foo, bar
        addAlarm(5, 5, OnmsSeverity.CLEARED);       // bar
        addAlarm(6, 6, OnmsSeverity.CRITICAL);

        addAlarm(7, 1, OnmsSeverity.MINOR);         // foo
        addAlarm(8, 2, OnmsSeverity.WARNING);       // foo, bar
        addAlarm(9, 3, OnmsSeverity.MINOR);         // bar

        addAlarm(10, null, OnmsSeverity.CRITICAL);  // -

        addAlarm(11, 2, OnmsSeverity.CRITICAL, true);  // -
    }

    @Test
    public void ifTttDaemonTestFoo() throws Exception {
        final Map<String, List<ResultEntry>> resultEntries = runIfTttDaemonTest(4, 4);

        List<ResultEntry> foo = resultEntries.get("Foo");
        List<ResultEntry> bar = resultEntries.get("Bar");
        List<ResultEntry> foobar = resultEntries.get("Foo|Bar");

        Assert.assertEquals(new ResultEntry("ON", "null", 0, "null", 0), foo.get(0));
        Assert.assertEquals(new ResultEntry("MINOR", "INDETERMINATE", 0, "MINOR", 5), foo.get(1));
        Assert.assertEquals(new ResultEntry("MAJOR", "MINOR", 5, "MAJOR", 6), foo.get(2));
        Assert.assertEquals(new ResultEntry("OFF", "null", 0, "null", 0), foo.get(3));
        Assert.assertEquals(new ResultEntry("ON", "null", 0, "null", 0), bar.get(0));
        Assert.assertEquals(new ResultEntry("CRITICAL", "INDETERMINATE", 0, "CRITICAL", 7), bar.get(1));
        Assert.assertEquals(new ResultEntry("CRITICAL", "CRITICAL", 7, "CRITICAL", 8), bar.get(2));
        Assert.assertEquals(new ResultEntry("OFF", "null", 0, "null", 0), bar.get(3));
        Assert.assertEquals(new ResultEntry("ON", "null", 0, "null", 0), foobar.get(0));
        Assert.assertEquals(new ResultEntry("MINOR", "INDETERMINATE", 0, "MINOR", 8), foobar.get(1));
        Assert.assertEquals(new ResultEntry("MAJOR", "MINOR", 8, "MAJOR", 9), foobar.get(2));
        Assert.assertEquals(new ResultEntry("OFF", "null", 0, "null", 0), foobar.get(3));
    }

    public Map<String, List<ResultEntry>> runIfTttDaemonTest(int timeout, int entryCount) throws Exception {
        final AlarmDao alarmDao = mock(AlarmDao.class);
        when(alarmDao.findMatching((Criteria) Matchers.anyObject())).thenReturn(alarmMap.values().stream().collect(Collectors.toList()));

        final TransactionOperations transactionOperations = mock(TransactionOperations.class);
        when(transactionOperations.execute(Matchers.anyObject())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                TransactionCallbackWithoutResult transactionCallbackWithoutResult = invocationOnMock.getArgumentAt(0, TransactionCallbackWithoutResult.class);
                transactionCallbackWithoutResult.doInTransaction(null);
                return null;
            }
        });

        //final List<ResultEntry> receivedEntries = new ArrayList<>();
        final Map<String, List<ResultEntry>> receivedEntries = new HashMap<>();

        final IfTttDaemon ifTttDaemon = new IfTttDaemon(alarmDao, transactionOperations, new File("src/test/resources/etc/ifttt-config.xml")) {
            @Override
            protected void fireIfTttTriggerSet(IfTttConfig ifTttConfig, String categoryFilter, String name, VariableNameExpansion variableNameExpansion) {
                if (!receivedEntries.containsKey(categoryFilter)) {
                    receivedEntries.put(categoryFilter, new ArrayList<>());
                }
                receivedEntries.get(categoryFilter).add(new ResultEntry(name, variableNameExpansion));
            }
        };

        ifTttDaemon.start();

        await().atMost(timeout, SECONDS).until(() -> allEntrySizesMatch(receivedEntries, entryCount - 2));
        LOG.debug("#1: {}", receivedEntries);

        addAlarm(12, 4, OnmsSeverity.MAJOR);
        when(alarmDao.findMatching((Criteria) Matchers.anyObject())).thenReturn(alarmMap.values().stream().collect(Collectors.toList()));

        await().atMost(timeout, SECONDS).until(() -> allEntrySizesMatch(receivedEntries, entryCount - 1));
        LOG.debug("#2: {}", receivedEntries);

        ifTttDaemon.stop();

        await().atMost(timeout, SECONDS).until(() -> allEntrySizesMatch(receivedEntries, entryCount));
        LOG.debug("#3: {}", receivedEntries);

        return receivedEntries;
    }

    private boolean allEntrySizesMatch(final Map<String, List<ResultEntry>> entries, final int expectedSize) {
        for (final Map.Entry<String, List<ResultEntry>> entry : entries.entrySet()) {
            if (entry.getValue().size() != expectedSize) {
                return false;
            }
        }

        return true;
    }
}
