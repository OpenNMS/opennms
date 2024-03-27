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

import java.util.List;

import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements NotificationStragey pattern used to send NULL notifications The
 * idea here is to allow for user assignment of a notice with in the UI with
 * out an email sent. Typically the email will be sent to a shared email box.
 *
 * @author <A HREF="mailto:Jason@Czerak.com">Jason Czerak</A>
 * @version $Id: $
 */
public class NullNotificationStrategy implements NotificationStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(NullNotificationStrategy.class);
    
    /**
     * <p>Constructor for NullNotificationStrategy.</p>
     */
    public NullNotificationStrategy() {
    }

    /** {@inheritDoc} */
    @Override
    public int send(List<Argument> arguments) {
        LOG.debug("In the NullNotification class.");
        return 0;
    }

}
