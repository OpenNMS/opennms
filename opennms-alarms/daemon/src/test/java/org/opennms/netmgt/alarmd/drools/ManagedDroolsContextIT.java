/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
