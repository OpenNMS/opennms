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

package org.opennms.core.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyListAdapter<T> extends XmlAdapter<List<T>, List<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(EmptyListAdapter.class);

    public EmptyListAdapter() {
        super();
        LOG.info("Initializing EmptyListAdapter.");
    }

    @Override
    public List<T> unmarshal(final List<T> in) throws Exception {
        final List<T> ret;
        if (in == null) {
            ret = new ArrayList<T>();
        } else {
            ret = in;
        }
        LOG.info("EmptyListAdapter.unmarshal(): returning {}", ret);
        return ret;
    }

    @Override
    public List<T> marshal(final List<T> out) throws Exception {
        final List<T> ret;
        if (out != null && out.size() == 0) {
            ret = null;
        } else {
            ret = out;
        }
        LOG.info("EmptyListAdapter.marshal(): returning {}", ret);
        return ret;
    }

}
