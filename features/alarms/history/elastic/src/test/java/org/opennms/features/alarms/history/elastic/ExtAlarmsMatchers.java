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
package org.opennms.features.alarms.history.elastic;

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.opennms.features.alarms.history.api.AlarmState;
import org.opennms.netmgt.model.OnmsSeverity;

public class ExtAlarmsMatchers {

    private static class HasSeverity extends TypeSafeMatcher<AlarmState> {
        private final OnmsSeverity severity;

        private HasSeverity(OnmsSeverity severity) {
            this.severity = severity;
        }

        @Override
        protected boolean matchesSafely(AlarmState alarm) {
            return Objects.equals(severity, OnmsSeverity.get(alarm.getSeverityId()));
        }

        @Override
        protected void describeMismatchSafely(AlarmState item, Description mismatchDescription) {
            mismatchDescription.appendText(String.format("was: %s", OnmsSeverity.get(item.getSeverityId())));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("alarm to have severity: " + severity);
        }
    }

    public static HasSeverity hasSeverity(OnmsSeverity severity) {
        return new HasSeverity(severity);
    }

    private static class Acknowledged extends TypeSafeMatcher<AlarmState> {
        @Override
        protected boolean matchesSafely(AlarmState alarm) {
            return alarm.getAckTime() != null && alarm.getAckUser() != null;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("acknowledged");
        }
    }

    public static Acknowledged acknowledged() {
        return new Acknowledged();
    }

    private static class Deleted extends TypeSafeMatcher<AlarmState> {
        @Override
        protected boolean matchesSafely(AlarmState alarm) {
            return alarm.getDeletedTime() != null;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("deleted");
        }
    }

    public static Deleted wasDeleted() {
        return new Deleted();
    }
}
