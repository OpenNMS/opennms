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
package org.opennms.features.situationfeedback.elastic;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedback.FeedbackType;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.dao.mock.MockTransactionManager;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.events.api.EventForwarder;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class ElasticFeedbackRepositoryIT {

    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";

    private FeedbackRepository feedbackRepository;

    @Rule
    public ElasticSearchRule elasticServerRule = new ElasticSearchRule();

    @Before
    public void setUp() throws MalformedURLException, ExecutionException, InterruptedException, FeedbackException {
        MockLogAppender.setupLogging(true, "DEBUG");
        final RestClientFactory restClientFactory = new RestClientFactory(elasticServerRule.getUrl());
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        final JestClientWithCircuitBreaker client = restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(ElasticFeedbackRepositoryIT.class.getName()), eventForwarder);
        final MockTransactionTemplate mockTransactionTemplate = new MockTransactionTemplate();
        mockTransactionTemplate.setTransactionManager(new MockTransactionManager());
        final IndexSettings settings = new IndexSettings();
        final ElasticFeedbackRepositoryInitializer initializer = new ElasticFeedbackRepositoryInitializer(client, settings);

        feedbackRepository = new ElasticFeedbackRepository(client, IndexStrategy.MONTHLY, 5, initializer);
        // initialize the repository manually
        initializer.initialize();
    }

    @Test
    public void canPersistAndRetrieveFeedback() throws FeedbackException {
        long now = System.currentTimeMillis();

        AlarmFeedback feedback1 = AlarmFeedback.newBuilder()
                .withSituationKey("situationKey1")
                .withSituationFingerprint("fingerprint1")
                .withAlarmKey("alarmKey1")
                .withFeedbackType(FeedbackType.FALSE_POSITIVE)
                .withReason("reason")
                .withRootCause(true)
                .withTags(Arrays.asList("red", "white", "blue"))
                .withUser("user")
                .withTimestamp(now)
                .build();
        AlarmFeedback feedback2 = AlarmFeedback.newBuilder()
                .withSituationKey("situationKey2")
                .withSituationFingerprint("fingerprint2")
                .withAlarmKey("alarmKey2")
                .withFeedbackType(FeedbackType.FALSE_POSITIVE)
                .withReason("reason")
                .withRootCause(false)
                .withTags(Arrays.asList("red", "blue", "blue"))
                .withUser("user")
                .withTimestamp(now + 1)
                .build();
        AlarmFeedback feedback3 = AlarmFeedback.newBuilder()
                .withSituationKey("situationKey3")
                .withSituationFingerprint("fingerprint3")
                .withAlarmKey("alarmKey3")
                .withFeedbackType(FeedbackType.FALSE_POSITIVE)
                .withReason("reason")
                .withRootCause(null)
                .withTags(Arrays.asList("red", "green", "blue"))
                .withUser("user")
                .withTimestamp(now + 2)
                .build();
        final List<AlarmFeedback> feedback = Arrays.asList(feedback1, feedback2, feedback3);
        feedbackRepository.persist(feedback);

        await().atMost(5, TimeUnit.SECONDS).until(() -> feedbackRepository.getFeedback("situationKey1"), hasSize(1));
        await().atMost(5, TimeUnit.SECONDS).until(() -> feedbackRepository.getFeedback("situationKey1").stream().map(AlarmFeedback::getRootCause).collect(Collectors.toList()),
                                                  equalTo(Arrays.asList(true)));
        await().atMost(5, TimeUnit.SECONDS).until(() -> feedbackRepository.getFeedback("situationKey3").stream().map(AlarmFeedback::getRootCause).collect(Collectors.toList()),
                                                  equalTo(Arrays.asList(false)));
        await().atMost(5, TimeUnit.SECONDS).until(() -> feedbackRepository.getFeedback("situationKey3").stream().map(AlarmFeedback::getTags).collect(Collectors.toList()),
                                                  equalTo(Arrays.asList(Arrays.asList("red", "green", "blue"))));
        await().atMost(5, TimeUnit.SECONDS).until(() -> feedbackRepository.getAllFeedback(), equalTo(feedback));
    }

}
