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
package org.opennms.web.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Whether a scheduled outage applies to any poller, collectd or threshd
 * package, and to notifications. Unlike {@link OutageApplicability} it holds
 * no package names, so it can be read by every role that reads the outage
 * itself (NMS-20579).
 */
@XmlRootElement(name = "outage-applicability-summary")
@XmlAccessorType(XmlAccessType.NONE)
public class OutageApplicabilitySummary {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "pollers")
    private boolean pollers;

    @XmlElement(name = "collectors")
    private boolean collectors;

    @XmlElement(name = "thresholders")
    private boolean thresholders;

    @XmlElement(name = "notifications")
    private boolean notifications;

    public OutageApplicabilitySummary() {
    }

    public OutageApplicabilitySummary(String name, boolean pollers, boolean collectors, boolean thresholders, boolean notifications) {
        this.name = name;
        this.pollers = pollers;
        this.collectors = collectors;
        this.thresholders = thresholders;
        this.notifications = notifications;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPollers() {
        return pollers;
    }

    public void setPollers(boolean pollers) {
        this.pollers = pollers;
    }

    public boolean isCollectors() {
        return collectors;
    }

    public void setCollectors(boolean collectors) {
        this.collectors = collectors;
    }

    public boolean isThresholders() {
        return thresholders;
    }

    public void setThresholders(boolean thresholders) {
        this.thresholders = thresholders;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    /**
     * The summary of every scheduled outage. The bare @XmlElement renders the
     * list as a JSON array under "outage", as GET /sched-outages does.
     */
    @XmlRootElement(name = "outage-applicability-summaries")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Summaries {

        @XmlElement(name = "outage")
        private List<OutageApplicabilitySummary> outages = new ArrayList<>();

        public List<OutageApplicabilitySummary> getOutages() {
            return outages;
        }

        public void setOutages(List<OutageApplicabilitySummary> outages) {
            this.outages = outages;
        }
    }
}
