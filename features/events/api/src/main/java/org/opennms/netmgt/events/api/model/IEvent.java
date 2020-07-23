/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api.model;

import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * A definition corresponding to POJO '{@link org.opennms.netmgt.xml.event.Event}'.
 *
 * The 'has...()' methods exist since the corresponding 'get...()' methods will return a default value if null.
 * Using the 'has...()' method is the only means to determine if the backing value is null.
 */
public interface IEvent {
    IAlarmData getAlarmData();
    IAutoAcknowledge getAutoacknowledge();
    IAutoAction getAutoaction(final int index);
    IAutoAction[] getAutoaction();
    List<IAutoAction> getAutoactionCollection();
    int getAutoactionCount();
    ICorrelation getCorrelation();
    Date getCreationTime();
    Integer getDbid();
    String getDescr();
    String getDistPoller();
    IForward getForward(final int index);
    IForward[] getForward();
    List<IForward> getForwardCollection();
    int getForwardCount();
    String getHost();
    String getIfAlias();
    Integer getIfIndex();
    String getInterface();
    InetAddress getInterfaceAddress();
    String getLoggroup(final int index);
    String[] getLoggroup();
    List<String> getLoggroupCollection();
    int getLoggroupCount();
    ILogMsg getLogmsg();
    IMask getMask();
    String getMasterStation();
    String getMouseovertext();
    Long getNodeid();
    IOperAction getOperaction(final int index);
    IOperAction[] getOperaction();
    List<IOperAction> getOperactionCollection();
    int getOperactionCount();
    String getOperinstruct();
    List<IParm> getParmCollection();
    IParm getParm(final String key);
    IParm getParmTrim(String key);
    String getPathoutage();
    IScript getScript(final int index);
    IScript[] getScript();
    List<IScript> getScriptCollection();
    int getScriptCount();
    String getService();
    String getSeverity();
    ISnmp getSnmp();
    String getSnmphost();
    String getSource();
    Date getTime();
    ITticket getTticket();
    String getUei();
    String getUuid();
    boolean hasDbid();
    boolean hasIfIndex();
    boolean hasNodeid();
    Enumeration<IAutoAction> enumerateAutoaction();
    String toStringSimple();
}
