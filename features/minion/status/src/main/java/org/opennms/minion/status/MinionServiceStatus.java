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
