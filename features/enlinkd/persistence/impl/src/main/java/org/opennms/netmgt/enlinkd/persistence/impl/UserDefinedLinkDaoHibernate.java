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
package org.opennms.netmgt.enlinkd.persistence.impl;

import java.util.List;

import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.enlinkd.persistence.api.UserDefinedLinkDao;

public class UserDefinedLinkDaoHibernate extends AbstractDaoHibernate<UserDefinedLink, Integer> implements UserDefinedLinkDao {
    public UserDefinedLinkDaoHibernate() {
        super(UserDefinedLink.class);
    }

    @Override
    public List<UserDefinedLink> getOutLinks(int nodeIdA) {
        return find("from UserDefinedLink udl where udl.nodeIdA = ?", nodeIdA);
    }

    @Override
    public List<UserDefinedLink> getInLinks(int nodeIdZ) {
        return find("from UserDefinedLink udl where udl.nodeIdZ = ?", nodeIdZ);
    }

    @Override
    public List<UserDefinedLink> getLinksWithLabel(String label) {
        return find("from UserDefinedLink udl where udl.linkLabel = ?", label);
    }
}
