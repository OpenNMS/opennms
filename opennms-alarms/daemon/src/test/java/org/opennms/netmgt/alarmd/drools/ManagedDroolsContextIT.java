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
package org.opennms.netmgt.alarmd.drools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;

public class ManagedDroolsContextIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    File rulesFolder;

    @Before
    public void setUp() throws IOException {
        rulesFolder = temporaryFolder.newFolder("rules.d");
    }

    /**
     * Verifies that we can load rules from the filesystem, start the context,
     * trigger, and stop the context successfully.
     */
    @Test
    public void canStartFireAndStop() throws IOException {
        generateSimpleRuleset();

        ManagedDroolsContext droolsCtx = new ManagedDroolsContext(rulesFolder, "test", "test");
        droolsCtx.setUseManualTick(true);

        NotificationService notificationService = new NotificationService();
        droolsCtx.setOnNewKiewSessionCallback(kieSession -> {
            kieSession.setGlobal("notificationService", notificationService);
        });
        droolsCtx.start();

        // No rules should have triggered yet
        assertThat(notificationService.getSent(), hasSize(equalTo(0)));

        Notification notifA = new Notification("oops");
        droolsCtx.getKieSession().insert(notifA);

        Notification notifB = new Notification("oh no");
        droolsCtx.getKieSession().insert(notifB);

        droolsCtx.tick();

        // A single rule should have triggered against "oops"
        assertThat(notificationService.getSent(), contains(notifA));

        droolsCtx.stop();

        // Validate that no more rules have been triggered
        assertThat(notificationService.getSent(), hasSize(equalTo(1)));
    }

    @Test
    public void canReloadRules() throws IOException {
        generateSimpleRuleset();

        ManagedDroolsContext droolsCtx = new ManagedDroolsContext(rulesFolder, "test", "test");
        droolsCtx.setUseManualTick(true);

        NotificationService notificationService = new NotificationService();
        droolsCtx.setOnNewKiewSessionCallback(kieSession -> {
            kieSession.setGlobal("notificationService", notificationService);
        });
        droolsCtx.start();

        // No rules should have triggered yet
        assertThat(notificationService.getSent(), hasSize(equalTo(0)));

        Notification notifA = new Notification("oops");
        droolsCtx.getKieSession().insert(notifA);

        Notification notifB = new Notification("oh no");
        droolsCtx.getKieSession().insert(notifB);

        droolsCtx.tick();

        // A single rule should have triggered against "oops"
        assertThat(notificationService.getSent(), contains(notifA));

        // Verify the number of facts
        assertThat(droolsCtx.getKieSession().getFactCount(), equalTo(3L));

        // Now let's update our ruleset
        generatedUpdatedRuleset();
        notificationService.clearSent();
        droolsCtx.reload();

        // Verify the number of facts
        assertThat(droolsCtx.getKieSession().getFactCount(), equalTo(3L));

        // Tick
        droolsCtx.tick();

        // A single rule should have triggered against "oh no"
        assertThat(notificationService.getSent(), contains(notifB));

        droolsCtx.stop();

        // Validate that no more rules have been triggered
        assertThat(notificationService.getSent(), hasSize(equalTo(1)));
    }

    private void generateSimpleRuleset() throws IOException {
        String rule = String.format("import %s;\n", Notification.class.getCanonicalName()) +
                String.format("global %s notificationService;\n", NotificationService.class.getCanonicalName()) +
                "rule \"fire!\"\n" +
                "  when\n" +
                "      $n : Notification( subject == \"oops\" )\n" +
                "  then\n" +
                "      notificationService.send($n);\n" +
                "end";
        Files.write(rulesFolder.toPath().resolve("simple.drl"), rule.getBytes(StandardCharsets.UTF_8));
    }

    private void generatedUpdatedRuleset() throws IOException {
        String rule = String.format("import %s;\n", Notification.class.getCanonicalName()) +
                String.format("global %s notificationService;\n", NotificationService.class.getCanonicalName()) +
                "rule \"fire!\"\n" +
                "  when\n" +
                "      $n : Notification( subject == \"oh no\" )\n" +
                "  then\n" +
                "      notificationService.send($n);\n" +
                "end";
        Files.write(rulesFolder.toPath().resolve("simple.drl"), rule.getBytes(StandardCharsets.UTF_8));
    }

    public static class Notification {
        private final String subject;

        public Notification(String subject) {
            this.subject = subject;
        }

        public String getSubject() {
            return subject;
        }
    }

    public static class NotificationService {
        private List<Notification> sent = new ArrayList<>();

        public synchronized void send(Notification notification) {
            sent.add(notification);
        }

        public synchronized List<Notification> getSent() {
            return Lists.newArrayList(sent);
        }

        public synchronized void clearSent() {
            sent.clear();
        }
    }
}
