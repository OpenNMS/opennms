/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
