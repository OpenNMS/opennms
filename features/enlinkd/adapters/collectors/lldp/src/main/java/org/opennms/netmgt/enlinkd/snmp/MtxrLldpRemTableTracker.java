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
package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpRowResult;

public class MtxrLldpRemTableTracker extends LldpRemTableTracker {

    public MtxrLldpRemTableTracker() {
        super();
    }

    public static class MtxrLldpRemRow extends LldpRemRow {

        public MtxrLldpRemRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

        public Integer getMtxrNeighborIndex() {
            return super.getLldpRemIndex();
        }

    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MtxrLldpRemRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processMtxrLldpRemRow((MtxrLldpRemRow)row);
    }

    /**
     * <p>processMtxrLldpRemRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker.LldpRemRow} object.
     */
    public void processMtxrLldpRemRow(final MtxrLldpRemRow row) {
        super.processLldpRemRow(row);
    }


}
