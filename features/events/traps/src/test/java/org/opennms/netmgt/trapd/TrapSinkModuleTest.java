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
package org.opennms.netmgt.trapd;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.model.OnmsDistPoller;

public class TrapSinkModuleTest {

    @Test
    public void testEqualsAndHashCode() throws Exception {
        SinkModule<Message, Message> mockModule = Mockito.mock(SinkModule.class);
        Mockito.when(mockModule.getId()).thenReturn("id");

        OnmsDistPoller distPollerMock = Mockito.mock(OnmsDistPoller.class);

        TrapdConfig config = new TrapdConfigBean();
        final TrapSinkModule module = new TrapSinkModule(config, distPollerMock);
        Assert.assertEquals(module, module);
        Assert.assertEquals(module.hashCode(), module.hashCode());

        final TrapSinkModule other = new TrapSinkModule(config, distPollerMock);
        Assert.assertEquals(module, other);
        Assert.assertEquals(module.hashCode(), other.hashCode());

        Assert.assertNotEquals(module, mockModule);
        Assert.assertNotEquals(module.hashCode(), mockModule.hashCode());
    }
}
