/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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
