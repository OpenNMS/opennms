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
 * Read-only summary of which subsystems a scheduled outage applies to: the
 * notifd on/off flag plus the poller, collectd and threshd packages, each
 * flagged with whether it currently references the outage. Populated by
 * reading the four subsystem configs; the UI uses it to render (and diff)
 * the "Applies To" matrix without four separate config reads.
 */
@XmlRootElement(name = "outage-applicability")
@XmlAccessorType(XmlAccessType.NONE)
public class OutageApplicability {

    @XmlElement(name = "notifications")
    private boolean notifications;

    // every outage name notifd references; with the per-package calendars this
    // lets the list page derive all memberships from one name-less call
    @XmlElement(name = "notification-calendars")
    private List<String> notificationCalendars = new ArrayList<>();

    // bare @XmlElement (no wrapper) so the v1 stack renders each list as a JSON
    // array under its own key, matching the Outages model the UI already reads
    @XmlElement(name = "pollers")
    private List<PackageRef> pollers = new ArrayList<>();

    @XmlElement(name = "collectors")
    private List<PackageRef> collectors = new ArrayList<>();

    @XmlElement(name = "thresholders")
    private List<PackageRef> thresholders = new ArrayList<>();

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public List<String> getNotificationCalendars() {
        return notificationCalendars;
    }

    public void setNotificationCalendars(List<String> notificationCalendars) {
        this.notificationCalendars = notificationCalendars;
    }

    public List<PackageRef> getPollers() {
        return pollers;
    }

    public void setPollers(List<PackageRef> pollers) {
        this.pollers = pollers;
    }

    public List<PackageRef> getCollectors() {
        return collectors;
    }

    public void setCollectors(List<PackageRef> collectors) {
        this.collectors = collectors;
    }

    public List<PackageRef> getThresholders() {
        return thresholders;
    }

    public void setThresholders(List<PackageRef> thresholders) {
        this.thresholders = thresholders;
    }

    @XmlRootElement(name = "package")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class PackageRef {

        @XmlAttribute(name = "name")
        private String name;

        @XmlAttribute(name = "applied")
        private boolean applied;

        @XmlElement(name = "calendars")
        private List<String> calendars = new ArrayList<>();

        public PackageRef() {
        }

        public PackageRef(String name, boolean applied, List<String> calendars) {
            this.name = name;
            this.applied = applied;
            if (calendars != null) {
                this.calendars = new ArrayList<>(calendars);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isApplied() {
            return applied;
        }

        public void setApplied(boolean applied) {
            this.applied = applied;
        }

        public List<String> getCalendars() {
            return calendars;
        }

        public void setCalendars(List<String> calendars) {
            this.calendars = calendars;
        }
    }
}
