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

import static org.opennms.netmgt.collection.api.CollectionResource.RESOURCE_TYPE_IF;
import static org.opennms.netmgt.collection.api.CollectionResource.RESOURCE_TYPE_NODE;

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


    @Test
    public void testCpuUtilization() throws ScriptException, IOException {

        var collectionAgent = Mockito.mock(CollectionAgent.class);
        Mockito.when(collectionAgent.getNodeId()).thenReturn(1);
        CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(collectionAgent);

        var url = this.getClass().getResource("/openconfig-gnmi-telemetry.groovy");
        if(url == null) {
            throw  new IllegalArgumentException("Invalid file path");
        }
        var file = new File(url.getPath());
        var scriptBuilder = new ScriptedCollectionSetBuilder(file);
        var collectionSet = scriptBuilder.build(collectionAgent, generateGnmiCpuUtilizationMessage(), Instant.now().toEpochMilli());
        var mockPersister = new MockPersister();
        collectionSet.visit(mockPersister);
        MatcherAssert.assertThat(mockPersister.getAttributeName(), CoreMatchers.is("cpu-utilization-state-avg"));
        MatcherAssert.assertThat(mockPersister.getValue(), CoreMatchers.is(15.0));

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

    private Gnmi.SubscribeResponse generateGnmiCpuUtilizationMessage() {
        Gnmi.SubscribeResponse.Builder builder = Gnmi.SubscribeResponse.newBuilder();
        
        Gnmi.Notification.Builder notificationBuilder = Gnmi.Notification.newBuilder();
        notificationBuilder.setTimestamp(1751293808127192960L);

        notificationBuilder.setPrefix(Gnmi.Path.newBuilder()
                .addElem(Gnmi.PathElem.newBuilder().setName("components").build())
                .addElem(Gnmi.PathElem.newBuilder().setName("component").putKey("name", "CPU0:CORE0").build())
                .build());

        notificationBuilder.addUpdate(Gnmi.Update.newBuilder()
                .setPath(Gnmi.Path.newBuilder()
                        .addElem(Gnmi.PathElem.newBuilder().setName("cpu").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName("utilization").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName("state").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName("avg").build())
                        .build())
                .setVal(Gnmi.TypedValue.newBuilder().setJsonVal(com.google.protobuf.ByteString.copyFromUtf8("15")).build())
                .build());
        
        builder.setUpdate(notificationBuilder.build());
        return builder.build();
    }


    private class MockPersister extends AbstractCollectionSetVisitor {

        private String interfaceLabel;
        private String attributeName;
        private Number value;

        @Override
        public void visitResource(CollectionResource resource) {
            if(resource.getResourceTypeName().equals(RESOURCE_TYPE_NODE)) {
               //setAttributeName(resource.getInstance());
            }
            if(resource.getResourceTypeName().equals(RESOURCE_TYPE_IF)) {
                setInterfaceLabel(resource.getInterfaceLabel());
            }

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
