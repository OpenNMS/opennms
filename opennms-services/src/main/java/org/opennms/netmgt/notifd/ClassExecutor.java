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
 * Implementation of Executor strategy that instantiates a Java class.
 *
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public class ClassExecutor implements ExecutorStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClassExecutor.class);
    
    /**
     * {@inheritDoc}
     *
     * This method calls the send method of the specified class in
     */
    @Override
    public int execute(String className, List<Argument> arguments) {
        LOG.debug("Going for the class instance: {}", className);
        NotificationStrategy ns;
        try {
            ns = (NotificationStrategy) Class.forName(className).newInstance();
            LOG.debug("{} class created: {}", className, ns.getClass());
        } catch (Throwable e) {
            LOG.error("Execption creating notification strategy class: {}", className, e);
            return 1;
        }

        try {
            return ns.send(arguments);
        } catch (Throwable t) {
            LOG.error("Throwable received while sending message", t);
            return 1;
        }
    }

}
