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
package org.opennms.features.apitokens.impl;

import java.util.List;

import org.opennms.features.apitokens.ApiToken;
import org.opennms.features.apitokens.ApiTokenDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;

public class ApiTokenDaoHibernate extends AbstractDaoHibernate<ApiToken, Integer> implements ApiTokenDao {

    public ApiTokenDaoHibernate() {
        super(ApiToken.class);
    }

    @Override
    public ApiToken findByTokenHash(String tokenHash) {
        return findUnique("from ApiToken where tokenHash = ?", tokenHash);
    }

    @Override
    public List<ApiToken> findByUsername(String username) {
        return find("from ApiToken where username = ? order by createdAt desc", username);
    }

    @Override
    public int countByUsername(String username) {
        return queryInt("select count(*) from ApiToken where username = ?", username);
    }

    @Override
    public void deleteByUsername(String username) {
        getHibernateTemplate().bulkUpdate("delete from ApiToken where username = ?", username);
    }

    @Override
    public String findUsernameByTokenId(Integer tokenId) {
        ApiToken token = get(tokenId);
        return token != null ? token.getUsername() : null;
    }
}
