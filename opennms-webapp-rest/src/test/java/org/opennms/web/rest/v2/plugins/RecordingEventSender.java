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
package org.opennms.web.rest.v2.plugins;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

class RecordingEventSender implements PluginEventSender {
    final List<Event> events = new ArrayList<>();

    @Override
    public void send(final Event event) {
        events.add(event);
    }

    List<String> ueis() {
        final List<String> ueis = new ArrayList<>();
        for (final Event e : events) {
            ueis.add(e.getUei());
        }
        return ueis;
    }

    Event last() {
        return events.get(events.size() - 1);
    }

    static String parm(final Event event, final String name) {
        for (final Parm parm : event.getParmCollection()) {
            if (name.equals(parm.getParmName())) {
                return parm.getValue() == null ? null : parm.getValue().getContent();
            }
        }
        return null;
    }
}
