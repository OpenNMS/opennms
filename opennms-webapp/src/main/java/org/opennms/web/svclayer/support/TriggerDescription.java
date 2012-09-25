/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.util.Date;

/**
 * <p>TriggerDescription class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class TriggerDescription {
    
    private String m_triggerName;
    private String m_description;
    private Date m_nextFireTime;
    
    /**
     * <p>getTriggerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTriggerName() {
        return m_triggerName;
    }
    
    /**
     * <p>setTriggerName</p>
     *
     * @param triggerName a {@link java.lang.String} object.
     */
    public void setTriggerName(String triggerName) {
        m_triggerName = triggerName;
    }
    
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     * <p>getNextFireTime</p>
     *
     * @return a java$util$Date object.
     */
    public Date getNextFireTime() {
        return m_nextFireTime;
    }

    /**
     * <p>setNextFireTime</p>
     *
     * @param nextFireTime a java$util$Date object.
     */
    public void setNextFireTime(Date nextFireTime) {
        m_nextFireTime = nextFireTime;
    }
    
    

}
