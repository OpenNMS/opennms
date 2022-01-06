/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.health.impl;

import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.Health;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.HealthCheckConstants;
import org.opennms.core.health.api.HealthCheckService;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.SimpleHealthCheck;
import org.opennms.core.health.api.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DefaultHealthCheckServiceTest {

    private static class BlockingHealthCheck implements HealthCheck {
        @Override
        public String getDescription() {
            return getClass().getSimpleName();
        }

        @Override
        public List<String> getTags() {
            return new ArrayList<>();
        }

        @Override
        public Response perform(Context context) {
            long start = System.currentTimeMillis();
            long spent = 0;
            while (spent < 5000) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                spent += System.currentTimeMillis() - start;
            }
            return new Response(Status.Success, "\\o/");
        }
    }

    private static class TagsHealthCheck implements HealthCheck {
        @Override
        public String getDescription() {
            return getClass().getSimpleName();
        }

        @Override
        public List<String> getTags() {
            return Arrays.asList(HealthCheckConstants.BUNDLE, HealthCheckConstants.BROKER);
        }

        @Override
        public Response perform(Context context) {
            return new Response(Status.Success, "\\o/");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHealthCheckServiceTest.class);

    @Rule
    public Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

    @Test
    public void verifyHealthCheckServiceDoesNotBlock() throws InvalidSyntaxException, ExecutionException, InterruptedException {
        final BlockingHealthCheck blockingHealthCheck = new BlockingHealthCheck();
        final SimpleHealthCheck successHealthCheck = new SimpleHealthCheck(() -> "Always green :)");
        successHealthCheck.markSucess();

        final DefaultHealthCheckService healthCheckService = new DefaultHealthCheckService(EasyMock.createNiceMock(BundleContext.class)) {
            @Override
            protected List<HealthCheck> getHealthChecks() {
                return Lists.newArrayList(blockingHealthCheck, successHealthCheck);
            }
        };

        final Context context = new Context();
        context.setTimeout(1000); // ms

        for (int i=0; i<2; i++) {
            var eitherErrorOrFuture = healthCheckService
                    .performAsyncHealthCheck(context,
                            new HealthCheckService.ProgressListener() {
                                @Override
                                public void onPerform(HealthCheck healthCheck) {
                                    LOG.info("Executing: {}", healthCheck.getDescription());
                                }

                                @Override
                                public void onResponse(HealthCheck check, Response response) {
                                    LOG.info("=> {} : {}", response.getStatus().name(), response.getMessage());
                                }
                            },
                            null);
            final Health health = eitherErrorOrFuture.get().toCompletableFuture().get();
            final List<Response> timedOutResponsed = health.getResponses().stream().map(Pair::getRight).filter(r -> r.getStatus() == Status.Timeout).collect(Collectors.toList());
            Assert.assertThat(timedOutResponsed.size(), is(1));
        }
    }

    @Test
    public void filterChecksWithTagsTest(){
        DefaultHealthCheckService healthCheckService = new DefaultHealthCheckService(EasyMock.createNiceMock(BundleContext.class));

        //both checks and tags are null
        List<HealthCheck> checks = null;
        List<String> tags = null;
        Assert.assertNull(healthCheckService.filterChecksWithTags(checks, tags));

        //checks null, tags non-null
        tags = new ArrayList<>();
        tags.add(HealthCheckConstants.BUNDLE);
        Assert.assertNull(healthCheckService.filterChecksWithTags(checks, tags));

        //tags null. checks non-null
        checks = new ArrayList<>();
        checks.add(new BlockingHealthCheck());
        tags = null;
        Assert.assertEquals(1, healthCheckService.filterChecksWithTags(checks, tags).size());

        //both checks and tags are non-null. Test filter.
        checks.add(new TagsHealthCheck());
        tags = new ArrayList<>();
        tags.add(HealthCheckConstants.BUNDLE);
        Assert.assertEquals(1, healthCheckService.filterChecksWithTags(checks, tags).size());

        //test tags has null value
        tags.add(null);
        Assert.assertEquals(1, healthCheckService.filterChecksWithTags(checks, tags).size());

        //test tags has empty value
        tags.add("");
        Assert.assertEquals(1, healthCheckService.filterChecksWithTags(checks, tags).size());
    }
}
