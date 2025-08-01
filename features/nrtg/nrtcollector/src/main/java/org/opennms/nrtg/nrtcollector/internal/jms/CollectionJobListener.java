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
package org.opennms.nrtg.nrtcollector.internal.jms;

import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.LightweightMeasurementSet;
import org.opennms.nrtg.api.model.MeasurementSet;
import org.opennms.nrtg.nrtcollector.internal.ProtocolCollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JMS Listener for {@link CollectionJob}-messages. Will call the
 * {@link ProtocolCollectorRegistry} to get a proper protocol-collector.
 * Starts the collection by a protocol-collector call.
 *
 * @author Simon Walter
 */
public class CollectionJobListener implements MessageListener {

    private static final Logger logger = LoggerFactory
            .getLogger(CollectionJobListener.class);

    private final SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter();

    private JmsTemplate jmsTemplate;

    private ProtocolCollectorRegistry protocolCollectorRegistry;

    private static AtomicInteger counter = new AtomicInteger(0);

    public CollectionJobListener() {
    }

    public CollectionJobListener(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public ProtocolCollectorRegistry getProtocolCollectorRegistry() {
        return protocolCollectorRegistry;
    }

    /**
     * @param protocolCollectorRegistry the protocolCollectorRegistry to set
     */
    public void setProtocolCollectorRegistry(
            ProtocolCollectorRegistry protocolCollectorRegistry) {
        this.protocolCollectorRegistry = protocolCollectorRegistry;
    }

    @Override
    public void onMessage(Message message) {

        try {

            // FIXME abstraction between technology and job handling is missing
            CollectionJob collectionJob = (CollectionJob) simpleMessageConverter.fromMessage(message);

            collectionJob = protocolCollectorRegistry.getProtocolCollector(collectionJob.getService()).collect(collectionJob);

            Date finishedTimestamp = new Date();

            collectionJob.setFinishedTimestamp(finishedTimestamp);

            Map<String, MeasurementSet> measurementSets = collectionJob.getMeasurementSetsByDestination();

            int val = counter.incrementAndGet();

            if (val % 1000 == 0) {
                logger.debug("processed job #{}, {} measurement set(s)", val, measurementSets.size());
            } else {
                logger.trace("processed job #{}, {} measurement set(s)", val, measurementSets.size());
            }

            for (String destinationString : measurementSets.keySet()) {
                jmsTemplate.convertAndSend(destinationString, measurementSets.get(destinationString));
                logger.info("** sending msg '{}' to '{}'", measurementSets.get(destinationString), destinationString);
            }

            LightweightMeasurementSet errorMeasurementSet = new LightweightMeasurementSet(
                    collectionJob.getNodeId(), collectionJob.getService(),
                    collectionJob.getNetInterface(),
                    collectionJob.getFinishedTimestamp());

            for (String metricId : collectionJob.getAllMetrics()) {

                if (collectionJob.getMetricValue(metricId) == null) {
                    errorMeasurementSet.addMeasurement(metricId, collectionJob.getMetricType(metricId), null, collectionJob.getOnmsLogicMetricId(metricId));
                }

                logger.trace("collected metric of job #{}='{}'", counter + ": "
                        + metricId, collectionJob.getMetricValue(metricId));
            }

            if (errorMeasurementSet.getMeasurements().size() > 0) {
                logger.warn("result set of job #{} contains {} null values",
                        counter, errorMeasurementSet.getMeasurements().size());
                jmsTemplate.convertAndSend("error", errorMeasurementSet);
                logger.trace("** sending to 'error'");
            }
        } catch (JMSException ex) {
            logger.error(ex.getMessage());
            // FIXME react, don't continue
        } catch (MessageConversionException ex) {
            logger.error(ex.getMessage());
            // FIXME react, don't continue
        }

    }
}
