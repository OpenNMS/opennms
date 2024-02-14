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
