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
package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.List;

import org.opennms.netmgt.model.notifd.Argument;
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
        
        LOG.debug("Dispatching microblog DM notification at base URL '{}' with destination user '{}' and message '{}'", svc.getConfiguration().getClientURL(), destUser, fullMessage);
        try {
            response = svc.sendDirectMessage(destUser, fullMessage);
        } catch (TwitterException e) {
            LOG.error("Microblog notification failed at service URL '{}' to destination user '{}'", svc.getConfiguration().getClientURL(), destUser, e);
            return 1;
        }
        
        LOG.info("Microblog DM notification succeeded: DM sent with ID {}", response.getId());
        return 0;
    }
}
