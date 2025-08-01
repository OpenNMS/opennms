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
package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;

public class ProvisionerRescanExistingDbonlyIT extends ProvisionerRescanExistingFalseIT {

    public void testNoRescanOnImport() throws Exception {
        executeTest("dbonly");
    }

    @Override
    protected void anticipateNoRescanSecondNodeEvents() {
        super.anticipateNoRescanSecondNodeEvents();
        
        final String name = this.getClass().getSimpleName();

        EventBuilder builder = new EventBuilder(EventConstants.NODE_UPDATED_EVENT_UEI, name);
        builder.setNodeid(1);
        builder.addParam(EventConstants.PARM_NODE_LABEL, "a");
        builder.addParam(EventConstants.PARM_NODE_LABEL_SOURCE, "U");
        builder.addParam(EventConstants.PARM_RESCAN_EXISTING, "false");
        m_eventAnticipator.anticipateEvent(builder.getEvent());
    }
    
}
