/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * <p>MicroblogDMNotificationStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org>Jeff Gehlbach</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 */
public class MicroblogDMNotificationStrategy extends MicroblogNotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(MicroblogDMNotificationStrategy.class);
    
    /**
     * <p>Constructor for MicroblogDMNotificationStrategy.</p>
     *
     * @throws java.io.IOException if any.
     */
    public MicroblogDMNotificationStrategy() throws IOException {
        super();
    }
    
    /**
     * <p>Constructor for MicroblogDMNotificationStrategy.</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public MicroblogDMNotificationStrategy(Resource configResource) {
        super(configResource);
    }
    
    /** {@inheritDoc} */
    @Override
    public int send(List<Argument> arguments) {
        Twitter svc = buildUblogService(arguments);
        String destUser = findDestName(arguments);
        DirectMessage response;

        if (destUser == null || "".equals(destUser)) {
            LOG.error("Cannot send a microblog DM notice to a user with no microblog username set. Either set a microblog username for this OpenNMS user or use the MicroblogUpdateNotificationStrategy instead.");
            return 1;
        }
        
        // In case the user tried to be helpful, avoid a double @@
        if (destUser.startsWith("@"))
            destUser = destUser.substring(1);
        
        String fullMessage = buildMessageBody(arguments);
        
        LOG.debug("Dispatching microblog DM notification for user '{}' at base URL '{}' with destination user '{}' and message '{}'", svc.getUserId(), svc.getBaseURL(), destUser, fullMessage);
        try {
            response = svc.sendDirectMessage(destUser, fullMessage);
        } catch (TwitterException e) {
            LOG.error("Microblog notification failed");
            LOG.info("Failed to send DM for user '{}' at service URL '{}' to destination user '{}', caught exception", svc.getUserId(), svc.getBaseURL(), destUser, e);
            return 1;
        }
        
        LOG.info("Microblog DM notification succeeded: DM sent with ID {}", response.getId());
        return 0;
    }
}
