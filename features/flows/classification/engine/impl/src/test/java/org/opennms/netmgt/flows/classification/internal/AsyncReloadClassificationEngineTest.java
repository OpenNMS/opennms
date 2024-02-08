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
package org.opennms.netmgt.flows.classification.internal;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
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
            private List<ClassificationRulesReloadedListener> classificationRulesReloadedListeners = new ArrayList<>();

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

            public void addClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
                this.classificationRulesReloadedListeners.add(classificationRulesReloadedListener);
            }

            public void removeClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
                this.classificationRulesReloadedListeners.remove(classificationRulesReloadedListener);
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
