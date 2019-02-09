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
