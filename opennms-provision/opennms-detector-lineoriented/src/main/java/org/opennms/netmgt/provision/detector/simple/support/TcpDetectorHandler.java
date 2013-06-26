/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple.support;

import org.apache.mina.core.session.IoSession;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.BaseDetectorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpDetectorHandler extends BaseDetectorHandler<LineOrientedRequest, LineOrientedResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(TcpDetectorHandler.class);
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Object request = getConversation().getRequest();
        if(!getConversation().hasBanner() &&  request != null) {
            session.write(request);
       }else if(!getConversation().hasBanner() && request == null) {
           LOG.info("TCP session was opened, no banner was expected, and there are no more pending requests. Setting service detection to true.");
           getFuture().setServiceDetected(true);
           session.close(true);
       }
    }

}
