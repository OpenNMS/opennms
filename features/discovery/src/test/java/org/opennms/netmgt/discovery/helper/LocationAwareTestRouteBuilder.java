/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery.helper;

import java.util.Objects;

import org.apache.camel.builder.RouteBuilder;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.netmgt.discovery.messages.DiscoveryJob;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;

/**
 * Is used to configure a route for a location if needed in tests.
 *
 * @author mvrueden
 */
public class LocationAwareTestRouteBuilder extends RouteBuilder {

    /**
     * Allows overwriting the result generation of the "discovery mock".
     */
    public interface JobProcessor {
        DiscoveryResults process(DiscoveryJob job) throws Exception;
    }

    private final String location;
    private final JobProcessor jobProcessor;

    public LocationAwareTestRouteBuilder(String location, JobProcessor jobProcessor) {
        this.location = Objects.requireNonNull(location);
        this.jobProcessor = Objects.requireNonNull(jobProcessor);
    }

    @Override
    public void configure() throws Exception {
        JmsQueueNameFactory factory = new JmsQueueNameFactory("Discovery", "Discoverer", location);
        String from = String.format("queuingservice:%s", factory.getName());

        from(from)
        .process(exchange -> {
            final DiscoveryJob job = exchange.getIn().getBody(DiscoveryJob.class);
            DiscoveryResults results = jobProcessor.process(job);
            exchange.getOut().setBody(results);
        });
    }
}
