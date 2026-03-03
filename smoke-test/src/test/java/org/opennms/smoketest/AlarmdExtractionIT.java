/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.smoketest.containers.AlarmdContainer;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestHealthClient;
import org.opennms.smoketest.utils.SshClient;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Smoke test that validates Alarmd can run as a standalone container, consuming
 * events from Kafka and creating alarms in PostgreSQL independently of the
 * core OpenNMS container.
 *
 * <p>Architecture under test:</p>
 * <pre>
 *   REST API → [OpenNMS Core] → Kafka → [Standalone Alarmd] → PostgreSQL
 *                                ↑ JMS (MessageBus) ↓
 * </pre>
 *
 * <p>Note: The core OpenNMS container still has its internal Alarmd active.
 * Both instances share the same PostgreSQL database and alarm reduction keys.
 * A future enhancement should disable the core Alarmd via service-configuration.xml
 * overlay to prove the standalone container is solely responsible for alarm creation.</p>
 */
public class AlarmdExtractionIT {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmdExtractionIT.class);

    private static final StackModel MODEL = StackModel.newBuilder()
            .withOpenNMS(OpenNMSProfile.newBuilder()
                    .withKafkaProducerEnabled(true)
                    .build())
            .build();

    private static final OpenNMSStack stack = OpenNMSStack.withModel(MODEL);

    private static final AlarmdContainer alarmd = new AlarmdContainer(MODEL);

    // Start the full stack first (PostgreSQL → Kafka → OpenNMS), then the standalone Alarmd
    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(stack)
            .around(alarmd);

    /**
     * Validates the end-to-end event→alarm pipeline through Kafka:
     * <ol>
     *   <li>Send a fault event to the Core container via REST</li>
     *   <li>OpenNMS publishes the event to Kafka (via Kafka producer)</li>
     *   <li>Verify an alarm is created in PostgreSQL</li>
     * </ol>
     */
    @Test
    public void shouldCreateAlarmFromKafkaEvent() {
        final Event event = new Event();
        event.setUei("uei.opennms.org/alarms/trigger");
        event.setSeverity("7");
        final List<Parm> parms = new ArrayList<>();
        parms.add(new Parm("service", "alarmd-extraction-test"));
        event.setParmCollection(parms);

        LOG.info("Sending alarm-triggering event to OpenNMS core...");
        stack.opennms().getRestClient().sendEvent(event);

        LOG.info("Waiting for alarm to appear in PostgreSQL...");
        final var alarmDao = stack.postgres().getDaoFactory().getDao(AlarmDaoHibernate.class);

        final OnmsAlarm alarm = await("alarm from Kafka event")
                .atMost(2, MINUTES)
                .pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(alarmDao,
                        new CriteriaBuilder(OnmsAlarm.class)
                                .eq("uei", "uei.opennms.org/alarms/trigger")
                                .toCriteria()), notNullValue());

        LOG.info("Alarm created successfully: id={}, reductionKey={}, severity={}",
                alarm.getId(), alarm.getReductionKey(), alarm.getSeverity());
    }

    /**
     * Validates that IPC messages reach the standalone Alarmd via the JMS MessageBus:
     * <ol>
     *   <li>Send a reloadDaemonConfig command for "alarmd" via the core Karaf shell</li>
     *   <li>Verify the standalone Alarmd container remains healthy after processing</li>
     * </ol>
     *
     * <p>The reload command flows: Core Karaf shell → EventIpcManager → Kafka →
     * Standalone Alarmd. Verification checks that the standalone container's health
     * check still passes after processing the reload, confirming the JMS connection
     * and daemon lifecycle management work correctly in the extracted container.</p>
     */
    @Test
    public void shouldReloadConfigViaMessageBus() throws Exception {
        LOG.info("Sending reloadDaemonConfig command for alarmd...");
        try (final SshClient sshClient = stack.opennms().ssh()) {
            final PrintStream pipe = sshClient.openShell();
            pipe.println("opennms:reload-daemon alarmd");
            pipe.println("logout");
            await("reload-daemon command")
                    .atMost(30, SECONDS)
                    .until(sshClient.isShellClosedCallable());
        }

        // Allow the reload message to propagate via JMS/Kafka to the standalone Alarmd
        LOG.info("Verifying standalone Alarmd remains healthy after reload...");
        final RestHealthClient healthClient = new RestHealthClient(alarmd.getWebUrl(), Optional.of("alarmd"));
        await("post-reload health check")
                .atMost(2, MINUTES)
                .pollInterval(10, SECONDS)
                .until(healthClient::getProbeHealthResponse, containsString(healthClient.getProbeSuccessMessage()));

        // Verify the Alarmd container's Karaf log doesn't show a crash or destruction
        final Path karafLog = Paths.get("/opt", "alarmd", "data", "log", "karaf.log");
        final String logContents = TestContainerUtils.getFileFromContainerAsString(alarmd, karafLog);
        LOG.info("Alarmd karaf.log size: {} bytes", logContents.length());
        alarmd.assertNoKarafDestroy(karafLog);

        LOG.info("Standalone Alarmd is healthy after reload.");
    }
}
