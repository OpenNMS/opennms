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
package org.opennms.features.kafka.producer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;

import com.google.protobuf.UnknownFieldSet;

public class CollectionSetProtoCompatibilityTest {

    @Test
    public void metricIdUsesNewFieldNumbers() {
        assertThat(CollectionSetProtos.NumericAttribute.METRIC_ID_FIELD_NUMBER, is(6));
        assertThat(CollectionSetProtos.StringAttribute.METRIC_ID_FIELD_NUMBER, is(3));
    }

    @Test
    public void unsetMetricIdDoesNotChangeEncoding() throws Exception {
        // Old producers' bytes parse with an empty metric_id; unset metric_id adds no bytes.
        var a = CollectionSetProtos.NumericAttribute.newBuilder().setGroup("g").setName("n").setValue(1.0).build();
        assertThat(CollectionSetProtos.NumericAttribute.parseFrom(a.toByteArray()).getMetricId(), is(""));
        assertThat(a.toBuilder().setMetricId("").build().toByteArray(), is(a.toByteArray()));
    }

    @Test
    public void metricIdIsLengthDelimitedFieldOldReadersSkip() throws Exception {
        var withId = CollectionSetProtos.NumericAttribute.newBuilder().setName("n").setMetricId(".1.3.6.1").build();
        var fields = UnknownFieldSet.parseFrom(withId.toByteArray());
        assertThat(fields.getField(6).getLengthDelimitedList().size(), is(1));
    }
}
