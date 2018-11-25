/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.daemon;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "daemonreloadstate")
@XmlAccessorType(XmlAccessType.NONE)
public class DaemonReloadInfo {
    @XmlAttribute(name = "ReloadRequestEventTime")
    private Long reloadRequestEventTime;

    @XmlAttribute(name = "ReloadResultEventTime")
    private Long reloadResultEventTime;

    @XmlAttribute(name = "reloadState")
    private DaemonReloadState reloadeState;

    public DaemonReloadInfo() {
    }

    public DaemonReloadInfo(Long requestEventTime, Long resultEventTime, DaemonReloadState reloadeState) {
        this.reloadRequestEventTime = requestEventTime;
        this.reloadResultEventTime = resultEventTime;
        this.reloadeState = reloadeState;
    }

    public Long getReloadRequestEventTime() {
        return reloadRequestEventTime;
    }

    public Long getReloadResultEventTime() {
        return reloadResultEventTime;
    }

    public DaemonReloadState getReloadState() {
        return reloadeState;
    }


    public void setReloadRequestEventTime(Long reloadRequestEventTime) {
        this.reloadRequestEventTime = reloadRequestEventTime;
    }

    public void setReloadResultEventTime(Long reloadResultEventTime) {
        this.reloadResultEventTime = reloadResultEventTime;
    }

    public void setReloadState(DaemonReloadState reloadeState) {
        this.reloadeState = reloadeState;
    }
}