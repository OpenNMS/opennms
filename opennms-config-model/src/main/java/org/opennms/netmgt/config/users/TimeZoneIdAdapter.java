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
package org.opennms.netmgt.config.users;

import java.time.DateTimeException;
import java.time.ZoneId;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeZoneIdAdapter extends XmlAdapter<String, ZoneId> {

    private final Logger LOG = LoggerFactory.getLogger(TimeZoneIdAdapter.class);

    @Override
    public String marshal(final ZoneId value) {
        return value == null ? null : value.getId();
    }

    @Override
    public ZoneId unmarshal(final String value) {
        if (value == null) {
            return null;
        }
        try {
            return ZoneId.of(value);
        } catch (DateTimeException e) {
            LOG.warn("can not unmarshal ZoneId=value", value);
            return null;
        }
    }
}