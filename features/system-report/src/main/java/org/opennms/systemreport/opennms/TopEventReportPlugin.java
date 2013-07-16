/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.systemreport.opennms;

import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.api.CountedObject;
import org.opennms.netmgt.dao.api.EventCountDao;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class TopEventReportPlugin extends AbstractSystemReportPlugin implements InitializingBean {
    @Autowired
    public EventCountDao m_eventCountDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Override
    public String getName() {
        return "TopEvent";
    }

    @Override
    public String getDescription() {
        return "Top 20 most reported events";
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();

        if (m_eventCountDao != null) {
            final Set<CountedObject<String>> objs = m_eventCountDao.getUeiCounts(20);
            for (final CountedObject<String> obj : objs) {
                map.put(obj.getObject(), new ByteArrayResource(obj.getCount().toString().getBytes()));
            }
        }
        return map;
    }

}
