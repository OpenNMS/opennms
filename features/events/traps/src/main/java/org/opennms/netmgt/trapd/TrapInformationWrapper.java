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

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.netmgt.snmp.SnmpException;
import org.opennms.netmgt.snmp.TrapInformation;

/**
 * Wrapper to make the {@link TrapInformation} object Sink API compatible, without adding the dependency to the sink-api module.
 *
 * @author mvrueden
 */
public class TrapInformationWrapper implements Message {

    private final TrapInformation trapInformation;

    private final TrapDTO trapDTO;

    private InetAddress trapAddress;

    public TrapInformationWrapper(TrapInformation trapInformation) throws SnmpException {
        this.trapInformation = Objects.requireNonNull(trapInformation);
        this.trapDTO = null;
        trapInformation.validate(); // Before this was at ProcessQueueProcessor which does not exist anymore
    }

    public TrapInformationWrapper(TrapDTO trapDTO) {
        this.trapDTO = trapDTO;
        this.trapInformation = null;
    }

    public TrapInformation getTrapInformation() {
        return trapInformation;
    }

    public TrapDTO getTrapDTO() {
        return trapDTO;
    }


    public InetAddress getTrapAddress() {
        if (trapInformation != null) {
            return getTrapInformation().getTrapAddress();
        }
        return trapAddress;
    }

    public void setTrapAddress(InetAddress trapAddress) {
        this.trapAddress = trapAddress;
    }

}
