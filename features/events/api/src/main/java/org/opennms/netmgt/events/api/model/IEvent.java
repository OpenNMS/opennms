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
    Long getDbid();
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
