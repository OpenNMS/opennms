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
package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.Comparator;

public class EventLabelComparator implements Comparator<Event>, Serializable {
    private static final long serialVersionUID = 7976730920523203921L;

    @Override
    public int compare(final Event e1, final Event e2) {
        if (e1.getEventLabel() == e2.getEventLabel()) return 0;
        if (e1.getEventLabel() == null) return -1;
        if (e2.getEventLabel() == null) return 1;
        return e1.getEventLabel().compareToIgnoreCase(e2.getEventLabel());
    }
}