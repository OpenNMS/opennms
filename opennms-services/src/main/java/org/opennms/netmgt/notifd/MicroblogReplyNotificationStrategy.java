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

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * <p>MicroblogReplyNotificationStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org>Jeff Gehlbach</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 */
public class MicroblogReplyNotificationStrategy extends MicroblogNotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(MicroblogReplyNotificationStrategy.class);

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
    public int send(final List<Argument> arguments) {
        final Twitter svc = buildUblogService(arguments);
        String destUser = findDestName(arguments);
        Status response;

        if (destUser == null || "".equals(destUser)) {
            LOG.error("Cannot send a microblog reply notice to a user with no microblog username set. Either set a microblog username for this OpenNMS user or use the MicroblogUpdateNotificationStrategy instead.");
            return 1;
        }
        
        // In case the user tried to be helpful, avoid a double @@
        if (destUser.startsWith("@"))
            destUser = destUser.substring(1);
        
        final String fullMessage = String.format("@%s %s", destUser, buildMessageBody(arguments));
        
        LOG.debug("Dispatching microblog reply notification at base URL '{}' with message '{}'", svc.getConfiguration().getClientURL(), fullMessage);
        try {
            response = svc.updateStatus(fullMessage);
        } catch (final TwitterException e) {
            LOG.error("Microblog notification failed at service URL '{}'", svc.getConfiguration().getClientURL(), e);
            return 1;
        }

        LOG.info("Microblog reply notification succeeded: reply update posted with ID {}", response.getId());
        return 0;
    }
    
}
