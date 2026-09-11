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
package org.opennms.netmgt.availability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.opennms.netmgt.dao.api.CategoryAvailabilityCalculator;
import org.opennms.netmgt.dao.api.CategoryAvailabilitySnapshotDao;
import org.opennms.netmgt.model.availability.CategoryAvailability;

public class AvailabilitySnapshotSchedulerTest {

    @Test
    public void nextDelayIsPacedAndClamped() {
        assertEquals(60_000L, AvailabilitySnapshotScheduler.nextDelayMillis(10, 60_000L, 900_000L, 5.0));
        assertEquals(100_000L, AvailabilitySnapshotScheduler.nextDelayMillis(20_000, 60_000L, 900_000L, 5.0));
        assertEquals(900_000L, AvailabilitySnapshotScheduler.nextDelayMillis(600_000, 60_000L, 900_000L, 5.0));
    }

    @Test
    public void effectiveRuleCombinesCommonAndCategoryRules() {
        assertEquals("(IPADDR != '0.0.0.0') & (isHTTP)", CategoriesXmlDefinitionProvider.effectiveRule("IPADDR != '0.0.0.0'", "isHTTP"));
        assertEquals("isHTTP", CategoriesXmlDefinitionProvider.effectiveRule(" ", "isHTTP"));
        assertEquals("IPADDR != '0.0.0.0'", CategoriesXmlDefinitionProvider.effectiveRule("IPADDR != '0.0.0.0'", null));
    }

    @Test
    public void refreshesEveryCategoryAndPrunesRemovedOnes() throws Exception {
        final CategoryDefinition web = new CategoryDefinition("Web Servers", "isHTTP", Arrays.asList("HTTP"));
        final CategoryDefinition mail = new CategoryDefinition("Email Servers", "isSMTP", Collections.emptyList());
        final CategoryDefinitionProvider provider = () -> Arrays.asList(web, mail);

        final CategoryAvailabilityCalculator calculator = mock(CategoryAvailabilityCalculator.class);
        when(calculator.calculate(any(), any(), anyList(), any(), any())).thenAnswer(invocation -> {
            final String label = invocation.getArgument(0);
            final Date start = invocation.getArgument(3);
            final Date end = invocation.getArgument(4);
            return new CategoryAvailability(label, start, end, new Date(), Collections.emptyList());
        });

        final CategoryAvailabilitySnapshotDao dao = mock(CategoryAvailabilitySnapshotDao.class);
        final CountDownLatch saved = new CountDownLatch(2);
        doAnswer(invocation -> { saved.countDown(); return null; }).when(dao).save(any());

        final AvailabilitySnapshotScheduler scheduler = new AvailabilitySnapshotScheduler(calculator, dao, provider);
        scheduler.setWindowMillis(TimeUnit.HOURS.toMillis(1));
        scheduler.setMinIntervalMillis(TimeUnit.MINUTES.toMillis(5));
        scheduler.setMaxIntervalMillis(TimeUnit.MINUTES.toMillis(10));
        scheduler.start();
        try {
            assertTrue("both categories should be refreshed on start", saved.await(10, TimeUnit.SECONDS));
        } finally {
            scheduler.stop();
        }

        verify(dao).retainOnly(Arrays.asList("Web Servers", "Email Servers"));
        verify(calculator).calculate(eq("Web Servers"), eq("isHTTP"), eq(Arrays.asList("HTTP")), any(), any());
        verify(calculator).calculate(eq("Email Servers"), eq("isSMTP"), eq(Collections.emptyList()), any(), any());
    }
}
