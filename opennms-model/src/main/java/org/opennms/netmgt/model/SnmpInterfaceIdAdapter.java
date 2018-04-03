/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SnmpInterfaceIdAdapter extends XmlAdapter<ArrayList<Integer>, Set<OnmsIpInterface>> {

    @Override
    public ArrayList<Integer> marshal(final Set<OnmsIpInterface> ifaces) throws Exception {
        final ArrayList<Integer> ret = new ArrayList<>();
        for (final OnmsIpInterface iface : ifaces) {
            ret.add(iface.getId());
        }
        return ret;
    }

    @Override
    public Set<OnmsIpInterface> unmarshal(final ArrayList<Integer> ids) throws Exception {
        final Set<OnmsIpInterface> ret = new TreeSet<>();
        for (final Integer id : ids) {
            final OnmsIpInterface iface = new OnmsIpInterface();
            iface.setId(id);
            ret.add(iface);
        }
        return ret;
    }

}
