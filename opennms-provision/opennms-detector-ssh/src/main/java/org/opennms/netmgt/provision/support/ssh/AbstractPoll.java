/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support.ssh;

import java.util.Collections;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;

/**
 * <p>Abstract AbstractPoll class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractPoll implements Poll {
    // default timeout of 3 seconds
    protected int m_timeout = 3000;
    
    /**
     * Set the timeout in milliseconds.
     *
     * @param milliseconds the timeout
     */
    public void setTimeout(int milliseconds) {
        m_timeout = milliseconds;
    }

    /**
     * Get the timeout in milliseconds.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * <p>poll</p>
     *
     * @param tracker a {@link org.opennms.core.utils.TimeoutTracker} object.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     * @throws org.opennms.netmgt.provision.support.ssh.InsufficientParametersException if any.
     */
    public abstract PollStatus poll(TimeoutTracker tracker) throws InsufficientParametersException;
    
    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     * @throws org.opennms.netmgt.provision.support.ssh.InsufficientParametersException if any.
     */
    @Override
    public PollStatus poll() throws InsufficientParametersException {
        Map<String,?> emptyMap = Collections.emptyMap();
        TimeoutTracker tracker = new TimeoutTracker(emptyMap, 1, getTimeout());
        return poll(tracker);
    }

}
