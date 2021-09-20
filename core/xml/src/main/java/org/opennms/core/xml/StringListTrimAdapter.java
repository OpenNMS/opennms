/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
