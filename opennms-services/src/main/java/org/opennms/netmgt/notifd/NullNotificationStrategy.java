/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.notifd.NotificationStrategy;

/**
 * Implements NotificationStragey pattern used to send NULL notifications The
 * idea here is to allow for user assignment of a notice with in the UI with
 * out an email sent. Typically the email will be sent to a shared email box.
 *
 * @author <A HREF="mailto:Jason@Czerak.com">Jason Czerak</A>
 * @version $Id: $
 */
public class NullNotificationStrategy implements NotificationStrategy {
    /**
     * <p>Constructor for NullNotificationStrategy.</p>
     */
    public NullNotificationStrategy() {
    }

    /** {@inheritDoc} */
    public int send(List<Argument> arguments) {
        log().debug("In the NullNotification class.");
        return 0;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
