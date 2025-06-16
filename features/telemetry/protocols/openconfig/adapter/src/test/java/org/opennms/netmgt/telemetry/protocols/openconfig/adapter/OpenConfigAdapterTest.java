/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class OpenConfigAdapterTest {

    private static final String INSTANCE_NAME = "abc-32/0/2";
    private static final String ATTRIBUTE_NAME = "in-forwarded-pkts";
    private static final long ATTRIBUTE_VALUE = 1000L;

    @Test
    public void testGroovyScriptForGnmi() throws ScriptException, IOException {

        var collectionAgent = Mockito.mock(CollectionAgent.class);
        CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(collectionAgent);

        var url = this.getClass().getResource("/openconfig-gnmi-telemetry.groovy");
        if(url == null) {
            throw  new IllegalArgumentException("Invalid file path");
        }
        var file = new File(url.getPath());
        var scriptBuilder = new ScriptedCollectionSetBuilder(file);
        var collectionSet = scriptBuilder.build(collectionAgent, generateGnmiMessage(), Instant.now().toEpochMilli());
        var mockPersister = new MockPersister();
        collectionSet.visit(mockPersister);
        MatcherAssert.assertThat(mockPersister.getInterfaceLabel(), CoreMatchers.is(INSTANCE_NAME));
        MatcherAssert.assertThat(mockPersister.getAttributeName(), CoreMatchers.containsString(ATTRIBUTE_NAME));
        MatcherAssert.assertThat(mockPersister.getValue(), CoreMatchers.is(ATTRIBUTE_VALUE));
    }

    private Gnmi.SubscribeResponse generateGnmiMessage() {
        Gnmi.SubscribeResponse.Builder builder = Gnmi.SubscribeResponse.newBuilder();
        Gnmi.Update.Builder updateBuilder = Gnmi.Update.newBuilder();
        updateBuilder.setPath(Gnmi.Path.newBuilder()
                        .addElem(Gnmi.PathElem.newBuilder().setName("ipv4").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName("state").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName("counters").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName(ATTRIBUTE_NAME).build()).build())
                .setVal(Gnmi.TypedValue.newBuilder().setUintVal(ATTRIBUTE_VALUE).build());
        builder.setUpdate(Gnmi.Notification.newBuilder()
                .setPrefix(Gnmi.Path.newBuilder()
                        .addElem(Gnmi.PathElem.newBuilder().setName("interfaces").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName("interface").putKey("name", INSTANCE_NAME).build())
                        .build())
                .addUpdate(updateBuilder).build());
        return builder.build();
    }
    
    private Gnmi.SubscribeResponse generateCustomGnmiMessage() {
            Gnmi.SubscribeResponse.Builder responseBuilder = Gnmi.SubscribeResponse.newBuilder();
            Gnmi.Path.Builder pathBuilder = Gnmi.Path.newBuilder()
                    .addElem(Gnmi.PathElem.newBuilder().setName("eth1").build())
                    .addElem(Gnmi.PathElem.newBuilder().setName("ifInOctets"));
            return responseBuilder.setUpdate(Gnmi.Notification.newBuilder().setTimestamp(System.currentTimeMillis())
                    .addUpdate(Gnmi.Update.newBuilder().setPath(pathBuilder.build())
                            .setVal(Gnmi.TypedValue.newBuilder().setUintVal(4000).build()).build())
                    .build()).build();
    }

    private class MockPersister extends AbstractCollectionSetVisitor {

        private String interfaceLabel;
        private String attributeName;
        private Number value;

        @Override
        public void visitResource(CollectionResource resource) {
            setInterfaceLabel(resource.getInterfaceLabel());
        }

        @Override
        public void visitAttribute(CollectionAttribute attribute) {
            setAttributeName(attribute.getName());
            setValue(attribute.getNumericValue());
        }

        public String getInterfaceLabel() {
            return interfaceLabel;
        }

        public void setInterfaceLabel(String interfaceLabel) {
            this.interfaceLabel = interfaceLabel;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public Number getValue() {
            return value;
        }

        public void setValue(Number value) {
            this.value = value;
        }
    }


}
