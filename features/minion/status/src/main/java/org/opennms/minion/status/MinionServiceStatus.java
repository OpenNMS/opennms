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

package org.opennms.minion.status;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class MinionServiceStatus implements MinionStatus, Comparable<MinionServiceStatus>, Serializable {
    private static final Comparator<MinionServiceStatus> COMPARATOR = Comparator.comparing(MinionServiceStatus::getState);

    private static final long serialVersionUID = 1L;

    private State m_state;

    MinionServiceStatus(final MinionStatus.State state) {
        m_state = state;
    }

    public static MinionServiceStatus up() {
        return new MinionServiceStatus(State.UP);
    }

    public static MinionServiceStatus down() {
        return new MinionServiceStatus(State.DOWN);
    }

    @Override
    public State getState() {
        return m_state;
    }

    @Override
    public boolean isUp() {
        return m_state == MinionStatus.UP;
    }

    @Override
    public String toString() {
        return m_state.toString();
    }

    @Override
    public int compareTo(final MinionServiceStatus o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null && o instanceof MinionServiceStatus) {
            final MinionServiceStatus status = (MinionServiceStatus)o;
            return Objects.equals(m_state, status.m_state);
        }
        return false;
    }
}
