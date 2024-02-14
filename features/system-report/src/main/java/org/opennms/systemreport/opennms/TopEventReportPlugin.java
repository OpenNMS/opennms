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
package org.opennms.systemreport.opennms;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.spring.BeanUtils;
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
    public Map<String, Resource> getEntries() {
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
