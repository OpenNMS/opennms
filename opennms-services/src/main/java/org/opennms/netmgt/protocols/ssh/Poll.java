/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 3, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.protocols.ssh;

import java.net.InetAddress;
import java.util.Collections;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.monitors.TimeoutTracker;
import org.opennms.netmgt.protocols.InsufficientParametersException;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 */
public class Poll extends Ssh implements org.opennms.netmgt.protocols.Poll {

    public Poll() { }
    
    public Poll(InetAddress address) {
        setAddress(address);
    }
    
    public Poll(InetAddress address, int port) {
        setAddress(address);
        setPort(port);
    }
    
    public Poll(InetAddress address, int port, int timeout) {
        setAddress(address);
        setPort(port);
        setTimeout(timeout);
    }

    public PollStatus poll(TimeoutTracker tracker) throws InsufficientParametersException {
        tracker.startAttempt();
        boolean isAvailable = connect();
        double responseTime = tracker.elapsedTimeInMillis();

        PollStatus ps = PollStatus.unavailable();
        
        String errorMessage = "";
        if (getError() != null) {
            errorMessage = getError().getMessage();
        }
        
        if (isAvailable) {
            ps = PollStatus.available(responseTime);
        } else if (errorMessage.matches("^.*java.net.NoRouteToHostException.*$")) {
            ps = PollStatus.unavailable("no route to host");
        } else if (errorMessage.matches("^.*(timeout: socket is not established|java.io.InterruptedIOException).*$")) {
            ps = PollStatus.unavailable("connection timed out");
        } else if (errorMessage.matches("^.*(connection is closed by foreign host|java.net.ConnectException).*$")) {
            ps = PollStatus.unavailable("connection exception");
        } else if (errorMessage.matches("^.*java.io.IOException.*$")) {
            ps = PollStatus.unavailable("I/O exception");
        }
        
        return ps;
    }
    
    public PollStatus poll() throws InsufficientParametersException {
        TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), 1, getTimeout());
        return poll(tracker);
        
    }

}
