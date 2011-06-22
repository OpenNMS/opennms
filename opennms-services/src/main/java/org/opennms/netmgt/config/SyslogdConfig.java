/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.UeiList;

/**
 * <p>SyslogdConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SyslogdConfig {
    /**
     * <p>getSyslogPort</p>
     *
     * @return a int.
     */
    public abstract int getSyslogPort();

    /**
     * <p>getListenAddress</p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.8.1
     */
    public abstract String getListenAddress();
    
    /**
     * <p>getNewSuspectOnMessage</p>
     *
     * @return a boolean.
     */
    public abstract boolean getNewSuspectOnMessage();

    /**
     * <p>getForwardingRegexp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getForwardingRegexp();

    /**
     * <p>getMatchingGroupHost</p>
     *
     * @return a int.
     */
    public abstract int getMatchingGroupHost();

    /**
     * <p>getMatchingGroupMessage</p>
     *
     * @return a int.
     */
    public abstract int getMatchingGroupMessage();

    /**
     * <p>getParser</p>
     *
     * @return the parser class to use when parsing syslog messages, as a string.
     */
    public abstract String getParser();

    /**
     * <p>getUeiList</p>
     *
     * @return a {@link org.opennms.netmgt.config.syslogd.UeiList} object.
     */
    public abstract UeiList getUeiList();

    /**
     * <p>getHideMessages</p>
     *
     * @return a {@link org.opennms.netmgt.config.syslogd.HideMessage} object.
     */
    public abstract HideMessage getHideMessages();
    
    /**
     * <p>getDiscardUei</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getDiscardUei();
}

