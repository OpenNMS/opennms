//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

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

