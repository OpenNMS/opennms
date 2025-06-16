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
package org.opennms.core.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringListTrimAdapter extends XmlAdapter<String[],List<String>> {
    private static final String[] TYPE = new String[0];

    @Override
    public String[] marshal(final List<String> value) throws Exception {
        if (value == null) return null;
        return value.stream().map(String::trim).collect(Collectors.toList()).toArray(TYPE);
    }

    @Override
    public List<String> unmarshal(final String[] value) throws Exception {
        if (value == null) return null;
        final List<String> ret = new ArrayList<>();
        for (final String v : value) {
            if (v == null) {
                ret.add(null);
            } else {
                ret.add(v.trim());
            }
        }
        return ret;
    }

}
