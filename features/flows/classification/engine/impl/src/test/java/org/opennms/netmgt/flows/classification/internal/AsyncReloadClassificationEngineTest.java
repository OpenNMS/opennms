/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class AsyncReloadClassificationEngineTest {

    @Test
    public void reloadsAreInterrupted() {

        var started = new AtomicInteger();
        var interrupted = new AtomicInteger();
        var sleep = new AtomicBoolean(true);
        var completed = new AtomicBoolean(false);

        ClassificationEngine ce = new ClassificationEngine() {
            @Override
            public String classify(ClassificationRequest classificationRequest) {
                return null;
            }

            @Override
            public List<Rule> getInvalidRules() {
                return null;
            }

            @Override
            public void reload() throws InterruptedException {
                started.incrementAndGet();
                try {
                    while (sleep.get()) Thread.sleep(1000);
                    completed.set(true);
                } catch (InterruptedException e) {
                    interrupted.incrementAndGet();
                    throw e;
                }
            }
        };

        var x = new AsyncReloadingClassificationEngine(ce);

        // first reload is triggered right away
        await().untilAsserted(() -> assertThat(started.get(), is(1)));
        await().untilAsserted(() -> assertThat(interrupted.get(), is(0)));

        x.reload();
        await().untilAsserted(() -> assertThat(started.get(), is(2)));
        // if a load process is under way then a reload triggers an interruption
        await().untilAsserted(() -> assertThat(interrupted.get(), is(1)));

        x.reload();
        await().untilAsserted(() -> assertThat(started.get(), is(3)));
        await().untilAsserted(() -> assertThat(interrupted.get(), is(2)));
        x.reload();
        await().untilAsserted(() -> assertThat(started.get(), is(4)));
        await().untilAsserted(() -> assertThat(interrupted.get(), is(3)));

        sleep.set(false);
        await().untilAsserted(() -> assertThat(completed.get(), is(true)));

        sleep.set(true);

        x.reload();
        await().untilAsserted(() -> assertThat(started.get(), is(5)));
        await().untilAsserted(() -> assertThat(interrupted.get(), is(3)));
        x.reload();
        await().untilAsserted(() -> assertThat(started.get(), is(6)));
        await().untilAsserted(() -> assertThat(interrupted.get(), is(4)));
        sleep.set(false);

    }
}
