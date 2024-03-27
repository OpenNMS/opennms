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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.transport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

public class Netflow9MessageBuilderTest {

    /**
     * Validate that the direction on the flow message is set to UNKNOWN if there is no value present
     */
    @Test
    public void verifyDefaultDirectionIsUnknown() {
        RecordEnrichment recordEnrichment = mock(RecordEnrichment.class);
        Netflow9MessageBuilder nf9MessageBuilder = new Netflow9MessageBuilder();
        FlowMessage.Builder builder = nf9MessageBuilder.buildMessage(Collections.emptyList(), recordEnrichment);
        assertThat(builder.getDirection(), equalTo(Direction.UNKNOWN));
    }
}
