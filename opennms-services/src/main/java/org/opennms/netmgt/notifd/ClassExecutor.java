/*******************************************************************************

 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.util.List;

import org.opennms.core.utils.Argument;
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
