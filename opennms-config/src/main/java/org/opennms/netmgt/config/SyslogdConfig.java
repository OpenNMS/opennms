/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.List;
import java.util.TimeZone;

import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;

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
    int getSyslogPort();

    /**
     * <p>getListenAddress</p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.8.1
     */
    String getListenAddress();
    
    /**
     * <p>getNewSuspectOnMessage</p>
     *
     * @return a boolean.
     */
    boolean getNewSuspectOnMessage();

    /**
     * <p>getForwardingRegexp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getForwardingRegexp();

    /**
     * <p>getMatchingGroupHost</p>
     *
     * @return a int.
     */
    Integer getMatchingGroupHost();

    /**
     * <p>getMatchingGroupMessage</p>
     *
     * @return a int.
     */
    Integer getMatchingGroupMessage();

    /**
     * <p>getParser</p>
     *
     * @return the parser class to use when parsing syslog messages, as a string.
     */
    String getParser();

    /**
     * A collection of Strings->UEI's
     */
    List<UeiMatch> getUeiList();

    /**
     * A collection of Strings we do not want to attach to the event.
     */
    List<HideMatch> getHideMessages();
    
    /**
     * <p>getDiscardUei</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getDiscardUei();

    /**
     * Number of threads used for consuming/dispatching messages.
     *
     * @return number of threads
     */
    int getNumThreads();

    /**
     * Maximum number of messages to keep in memory while waiting
     * to be dispatched.
     *
     * @return queue size
     */
    int getQueueSize();

    /**
     * Messages are aggregated in batches before being dispatched.
     *
     * When the batch reaches this size, it will be dispatched.
     *
     * @return batch size
     */
    int getBatchSize();

    /**
     * Messages are aggregated in batches before being dispatched.
     *
     * When the batch has been created for longer than this interval
     * it will be dispatched, regardless of the size.
     *
     * @return interval in ms
     */
    int getBatchIntervalMs();

    /**
     * Optional:
     * - if not null it will be used as default time zone if no time zone is given
     * - if not set the system time zone will be used
     */
    TimeZone getTimeZone();
}
