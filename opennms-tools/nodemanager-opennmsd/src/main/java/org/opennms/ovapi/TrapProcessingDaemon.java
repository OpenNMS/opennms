/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.ovapi;

import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpSession;
import org.opennms.nnm.swig.SnmpCallback;
import org.opennms.nnm.swig.fd_set;
import org.opennms.nnm.swig.timeval;

public abstract class TrapProcessingDaemon extends OVsDaemon {
    
    OVsnmpSession m_trapSession;
    
    protected String onInit() {
        
        SnmpCallback trapCB = new SnmpCallback() {

            public void callback(int reason, OVsnmpSession session, OVsnmpPdu pdu) {
                onEvent(reason, session, pdu);
            }
        };
        
        m_trapSession = OVsnmpSession.eventOpen("opennmsd", trapCB, ".*");

        return "TrapProcessingDaemon has initialized successfully.";
    }

    protected abstract void onEvent(int reason, OVsnmpSession session, OVsnmpPdu pdu);

    protected String onStop() {
        
        
        m_trapSession.close();

        return "TrapProcessingDaemon has exited successfully.";
    }

    protected int getRetryInfo(fd_set fdset, timeval tm) {
        int maxSnmpFDs = OVsnmpSession.getRetryInfo(fdset, tm);
        int maxSuperFDs = super.getRetryInfo(fdset, tm);
        return Math.max(maxSnmpFDs, maxSuperFDs);
    }

    protected void processReads(fd_set fdset) {
        OVsnmpSession.read(fdset);
        super.processReads(fdset);
    }

    protected void processTimeouts() {
        OVsnmpSession.doRetry();
        super.processTimeouts();
    }

    
}
