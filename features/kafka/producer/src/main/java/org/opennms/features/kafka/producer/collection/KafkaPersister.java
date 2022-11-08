/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer.collection;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.features.kafka.producer.model.CollectionSetProtos.CollectionSetResource;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class KafkaPersister implements Persister {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPersister.class);

    private final int MAX_BUFFER_SIZE_CONFIGURED = 921600;

    private CollectionSetMapper collectionSetMapper;

    private final ServiceParameters m_params;

    private KafkaProducer<String, byte[]> producer;
    
    private String topicName = "metrics";

    public KafkaPersister(ServiceParameters params) {
        m_params = params;
    }

    public KafkaPersister() {
        m_params = new ServiceParameters(Collections.emptyMap());
    }

    /** {@inheritDoc} */
    @Override
    public void visitCollectionSet(CollectionSet collectionSet) {


        CollectionSetProtos.CollectionSet collectionSetProto = collectionSetMapper
                .buildCollectionSetProtos(collectionSet, m_params);
        if (collectionSetProto != null) {
            bisectAndSendMessageToKafka(collectionSetProto);
        }
    }

    void bisectAndSendMessageToKafka(CollectionSetProtos.CollectionSet collectionSetProto) {

        if (checkForMaxSize(collectionSetProto.toByteArray().length)) {

            if(collectionSetProto.getResourceCount() == 1) {
                /// Handle the case where resource is only one with too many attributes that can cross max buffer size.
                CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
                if(collectionSetResource.getNumericList().size() > 0) {
                    // Handle numeric attributes only.
                    CollectionSetProtos.CollectionSetResource.Builder numericResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
                    numericResourceBuilder.mergeFrom(collectionSetResource).clearString();
                    CollectionSetProtos.CollectionSet collectionSetWithNumeric = CollectionSetProtos.CollectionSet.newBuilder()
                            .addResource(numericResourceBuilder).setTimestamp(collectionSetProto.getTimestamp()).build();
                    bisectNumericAttributes(collectionSetWithNumeric);
                }
                if(collectionSetResource.getStringList().size() > 0) {
                    // Handle string attributes only
                    CollectionSetProtos.CollectionSetResource.Builder stringResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
                    stringResourceBuilder.mergeFrom(collectionSetResource).clearNumeric();
                    CollectionSetProtos.CollectionSet collectionSetWithStringAttributes = CollectionSetProtos.CollectionSet.newBuilder()
                            .addResource(stringResourceBuilder).setTimestamp(collectionSetProto.getTimestamp()).build();
                    bisectStringAttributes(collectionSetWithStringAttributes);
                }
            } else {
                // Divide resources into two in recursive way.
                Iterator<List<CollectionSetResource>> subList = Iterables.partition(collectionSetProto.getResourceList(),
                        (collectionSetProto.getResourceCount() + 1) / 2).iterator();

                CollectionSetProtos.CollectionSet firstPartCollectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                        .mergeFrom(collectionSetProto).clearResource().addAllResource(subList.next()).build();
                bisectAndSendMessageToKafka(firstPartCollectionSet);

                CollectionSetProtos.CollectionSet secondPartCollectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                        .mergeFrom(collectionSetProto).clearResource().addAllResource(subList.next()).build();
                bisectAndSendMessageToKafka(secondPartCollectionSet);
            }
        } else {
            sendMessageToKafka(collectionSetProto);
        }
    }

    private void bisectNumericAttributes(CollectionSetProtos.CollectionSet collectionSetProto) {
        // Divide numeric attributes into two in recursive way
        if (checkForMaxSize(collectionSetProto.toByteArray().length)) {
            Iterator<List<CollectionSetProtos.NumericAttribute>> subList = Iterables.partition(collectionSetProto.getResource(0).getNumericList(),
                    (collectionSetProto.getResource(0).getNumericCount() + 1) / 2).iterator();
            bisectNumericAttributes(buildCollectionSetWithNumericAttributes(collectionSetProto, subList.next()));
            bisectNumericAttributes(buildCollectionSetWithNumericAttributes(collectionSetProto, subList.next()));
        } else {
            sendMessageToKafka(collectionSetProto);
        }
    }

    private CollectionSetProtos.CollectionSet buildCollectionSetWithNumericAttributes(CollectionSetProtos.CollectionSet originalCollectionSet,
                                                                                      List<CollectionSetProtos.NumericAttribute> numericAttributes) {

        CollectionSetProtos.CollectionSet.Builder collectionSetBuilder = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(originalCollectionSet.getTimestamp());
        CollectionSetProtos.CollectionSetResource.Builder collectionSetResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
        collectionSetResourceBuilder.mergeFrom(originalCollectionSet.getResource(0)).clearNumeric().addAllNumeric(numericAttributes);
        collectionSetBuilder.addResource(collectionSetResourceBuilder);
        return collectionSetBuilder.build();
    }

    private void bisectStringAttributes(CollectionSetProtos.CollectionSet collectionSetProto) {
        // Divide string attributes into two in recursive way
        if (checkForMaxSize(collectionSetProto.toByteArray().length)) {
            Iterator<List<CollectionSetProtos.StringAttribute>> subList = Iterables.partition(collectionSetProto.getResource(0).getStringList(),
                    (collectionSetProto.getResource(0).getStringCount() + 1) / 2).iterator();
            bisectStringAttributes(buildCollectionSetWithStringAttributes(collectionSetProto, subList.next()));
            bisectStringAttributes(buildCollectionSetWithStringAttributes(collectionSetProto, subList.next()));
        } else {
            sendMessageToKafka(collectionSetProto);
        }
    }

    private CollectionSetProtos.CollectionSet buildCollectionSetWithStringAttributes(CollectionSetProtos.CollectionSet originalCollectionSet,
                                                                                      List<CollectionSetProtos.StringAttribute> stringAttributes) {

        CollectionSetProtos.CollectionSet.Builder collectionSetBuilder = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(originalCollectionSet.getTimestamp());
        CollectionSetProtos.CollectionSetResource.Builder collectionSetResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
        collectionSetResourceBuilder.mergeFrom(originalCollectionSet.getResource(0)).clearString().addAllString(stringAttributes);
        collectionSetBuilder.addResource(collectionSetResourceBuilder);
        return collectionSetBuilder.build();
    }

    boolean checkForMaxSize(int length) {
        return length > MAX_BUFFER_SIZE_CONFIGURED;
    }
    
    private void sendMessageToKafka( CollectionSetProtos.CollectionSet collectionSetProto) {
        // If no resources should be persisted, do not send an empty CollectionSet
        if (collectionSetProto.getResourceCount() == 0) {
            return;
        }
        // Derive key, it will be nodeId for all resources except for response time, it would be IpAddress
        final String key = deriveKeyFromCollectionSet(collectionSetProto);
        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topicName, key,
                collectionSetProto.toByteArray());
        producer.send(record, (recordMetadata, e) -> {
            if (e != null) {
                LOG.warn("Failed to send record to producer: {}.", record, e);
                return;
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("persisted collection {} to kafka with key {}", collectionSetProto.toString(), key);
            }
        });
    }

    private String deriveKeyFromCollectionSet(CollectionSetProtos.CollectionSet collectionSetProto) {
        String key = "";
        if (collectionSetProto.getResourceCount() > 0) {
            CollectionSetResource firstResource = collectionSetProto.getResource(0);
            if (firstResource.hasResponse()) {
                // For response time resources, key will be instance i.e. IpAddress
                key = firstResource.getResponse().getInstance();
            } else if (firstResource.hasInterface()) {
                key = Long.toString(firstResource.getInterface().getNode().getNodeId());
            } else if (firstResource.hasGeneric()) {
                key = Long.toString(firstResource.getGeneric().getNode().getNodeId());
            } else if (firstResource.hasNode()) {
                key = Long.toString(firstResource.getNode().getNodeId());
            } 
        }
        return key;
    }

    public void setTopicName(String topicName) {
        if (!Strings.isNullOrEmpty(topicName)) {
            this.topicName = topicName;
        }
    }

    public void setProducer(KafkaProducer<String, byte[]> producer) {
        this.producer = producer;
    }

    public void setCollectionSetMapper(CollectionSetMapper collectionSetMapper) {
        this.collectionSetMapper = collectionSetMapper;
    }

    @Override
    public void visitResource(CollectionResource resource) {
        // not handled here
    }

    @Override
    public void visitGroup(AttributeGroup group) {
        // not handled here
    }

    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        // not handled here
    }

    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        // not handled here
    }

    @Override
    public void completeGroup(AttributeGroup group) {
        // not handled here
    }

    @Override
    public void completeResource(CollectionResource resource) {
        // not handled here
    }

    @Override
    public void completeCollectionSet(CollectionSet set) {
        // not handled here
    }

    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        // not handled here
    }

    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        // not handled here
    }

}
