/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class EscapeSequenceAdapter extends XmlAdapter<String, String> {
    private static final Logger LOG = LoggerFactory.getLogger(EscapeSequenceAdapter.class);

    public EscapeSequenceAdapter() {

    }

    @Override
    public String unmarshal(String v) throws Exception {
        if (v != null) {
            v = v.replace("&#xd;", "\r")
                    .replace("&#xa;", "\n");
        }
            return v;

    }

    @Override
    public String marshal(String v) throws Exception {
        if (v != null) {
            v = v.replace("\r", "&#xd;")
                    .replace("\n", "&#xa;");
        }
            return v;

    }
}
