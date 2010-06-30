/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 25, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.springframework.core.io.Resource;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * <p>MicroblogReplyNotificationStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org>Jeff Gehlbach</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 * @author <a href="mailto:jeffg@opennms.org>Jeff Gehlbach</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 * @version $Id: $
 */
public class MicroblogReplyNotificationStrategy extends MicroblogNotificationStrategy {
    
    /**
     * <p>Constructor for MicroblogReplyNotificationStrategy.</p>
     *
     * @throws java.io.IOException if any.
     */
    public MicroblogReplyNotificationStrategy() throws IOException {
        super();
    }
    
    /**
     * <p>Constructor for MicroblogReplyNotificationStrategy.</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public MicroblogReplyNotificationStrategy(Resource configResource) {
        super(configResource);
    }

    /** {@inheritDoc} */
    @Override
    public int send(List<Argument> arguments) {
        Twitter svc = buildUblogService(arguments);
        String destUser = findDestName(arguments);
        Status response;

        if (destUser == null || "".equals(destUser)) {
            log().error("Cannot send a microblog reply notice to a user with no microblog username set. Either set a microblog username for this OpenNMS user or use the MicroblogUpdateNotificationStrategy instead.");
            return 1;
        }
        
        // In case the user tried to be helpful, avoid a double @@
        if (destUser.startsWith("@"))
            destUser = destUser.substring(1);
        
        String fullMessage = String.format("@%s %s", destUser, buildMessageBody(arguments));
        
        if (log().isDebugEnabled()) {
            log().debug("Dispatching microblog reply notification for user '" + svc.getUserId() + "' at base URL '" + svc.getBaseURL() + "' with message '" + fullMessage + "'");
        }
        try {
            response = svc.updateStatus(fullMessage);
        } catch (TwitterException e) {
            log().error("Microblog notification failed");
            log().info("Failed to update status for user '" + svc.getUserId() + "' at service URL '" + svc.getBaseURL() + "', caught exception: " + e.getMessage());
            return 1;
        }
        
        log().info("Microblog reply notification succeeded: reply update posted with ID " + response.getId());
        return 0;
    }
    
}
