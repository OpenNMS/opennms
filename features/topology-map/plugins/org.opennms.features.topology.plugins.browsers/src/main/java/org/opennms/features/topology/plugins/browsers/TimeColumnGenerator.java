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
package org.opennms.features.topology.plugins.browsers;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import org.opennms.features.timeformat.api.TimeformatService;
import org.opennms.vaadin.user.UserTimeZoneExtractor;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;

public class TimeColumnGenerator  implements Table.ColumnGenerator {

    private TimeformatService timeformatService;

    public TimeColumnGenerator(TimeformatService timeformatService) {
        this.timeformatService = timeformatService;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        final ZoneId userTimeZoneId =  UserTimeZoneExtractor.extractUserTimeZoneIdOrNull(source.getUI());
        final Property property = source.getContainerProperty(itemId, columnId);
        if (property == null || property.getValue() == null) {
            return null;
        }
        String formattedValue;
        if(property.getType().equals(Instant.class)){
            formattedValue = timeformatService.format((Instant) property.getValue(), userTimeZoneId);
        } else if(property.getType().equals(Date.class)){
            formattedValue = timeformatService.format((Date) property.getValue(), userTimeZoneId);
        } else {
            formattedValue = property.toString();
        }
        return formattedValue;
    }
}
