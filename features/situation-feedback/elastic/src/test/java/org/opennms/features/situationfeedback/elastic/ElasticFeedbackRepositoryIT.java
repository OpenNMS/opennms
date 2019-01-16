/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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
package org.opennms.features.situationfeedback.elastic;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedback.FeedbackType;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.netmgt.dao.mock.MockTransactionManager;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.plugins.elasticsearch.rest.RestClientFactory;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.opennms.plugins.elasticsearch.rest.template.IndexSettings;

import io.searchbox.client.JestClient;

public class ElasticFeedbackRepositoryIT {

    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";

    private FeedbackRepository feedbackRepository;

    @Rule
    public ElasticSearchRule elasticServerRule = new ElasticSearchRule(
            new ElasticSearchServerConfig()
                    .withDefaults()
                    .withSetting("http.enabled", true)
                    .withSetting("http.port", HTTP_PORT)
                    .withSetting("http.type", "netty4")
                    .withSetting("transport.type", "netty4")
                    .withSetting("transport.tcp.port", HTTP_TRANSPORT_PORT)
    );

    @Before
    public void setUp() throws MalformedURLException, ExecutionException, InterruptedException, FeedbackException {
        MockLogAppender.setupLogging(true, "DEBUG");
        final RestClientFactory restClientFactory = new RestClientFactory("http://localhost:" + HTTP_PORT, null, null);
        final JestClient client = restClientFactory.createClient();
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
                .withUser("user")
                .withTimestamp(now)
                .build();
        AlarmFeedback feedback2 = AlarmFeedback.newBuilder()
                .withSituationKey("situationKey2")
                .withSituationFingerprint("fingerprint2")
                .withAlarmKey("alarmKey2")
                .withFeedbackType(FeedbackType.FALSE_POSITIVE)
                .withReason("reason")
                .withUser("user")
                .withTimestamp(now + 1)
                .build();
        AlarmFeedback feedback3 = AlarmFeedback.newBuilder()
                .withSituationKey("situationKey3")
                .withSituationFingerprint("fingerprint3")
                .withAlarmKey("alarmKey3")
                .withFeedbackType(FeedbackType.FALSE_POSITIVE)
                .withReason("reason")
                .withUser("user")
                .withTimestamp(now + 2)
                .build();
        final List<AlarmFeedback> feedback = Arrays.asList(feedback1, feedback2, feedback3);
        feedbackRepository.persist(feedback);

        await().until(() -> feedbackRepository.getFeedback("situationKey1"), hasSize(1));
        await().until(() -> feedbackRepository.getAllFeedback(), equalTo(feedback));
    }

}
