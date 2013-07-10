/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.opennmsd;

import java.util.Date;

import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.ovapi.OVsnmpPduUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNNMEvent implements NNMEvent {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultNNMEvent.class);

    public DefaultNNMEvent(OVsnmpPdu trap) {
        log.debug(OVsnmpPduUtils.toString(trap));
        trap.free();
    }

    public String getCategory() {
        return "Category";
    }

    public String getName() {
        return "Name";
    }

    public String getSeverity() {
        return "Severity";
    }

    public String getSourceAddress() {
        return "192.168.1.1";
    }

    public Date getTimeStamp() {
        return new Date();
    }
    
    


}
