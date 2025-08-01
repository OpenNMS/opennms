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
package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliasBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(AliasBuilder.class);

    final Map<String, Alias> m_aliases = new HashMap<String, Alias>();

    public final AliasBuilder alias(final String associationPath, final String alias, final JoinType type, final Restriction joinCondition) {
        Alias existing = m_aliases.get(alias);
        if (existing == null) {
            if (joinCondition == null) {
                m_aliases.put(alias, new Alias(associationPath, alias, type));
            } else {
                m_aliases.put(alias, new Alias(associationPath, alias, type, joinCondition));
            }
        } else {
            if (joinCondition == null) {
                LOG.debug("alias '{}' already associated with associationPath '{}', skipping.", alias, associationPath);
            } else {
                if (existing.hasJoinCondition()) {
                    // Combine the JOIN conditions
                    LOG.debug("alias '{}' already associated with associationPath '{}', appending join condition.", alias, associationPath);
                    existing.setJoinCondition(Restrictions.and(existing.getJoinCondition(), joinCondition));
                } else {
                    LOG.debug("alias '{}' already associated with associationPath '{}', adding join condition.", alias, associationPath);
                    existing.setJoinCondition(joinCondition);
                }
            }
        }
        return this;
    }

    public final AliasBuilder alias(final Alias alias) {
        Alias existing = m_aliases.get(alias.getAlias());
        if (existing == null) {
            m_aliases.put(alias.getAlias(), alias);
        } else {
            if (alias.hasJoinCondition()) {
                if (existing.hasJoinCondition()) {
                    // Combine the JOIN conditions
                    LOG.debug("alias '{}' already associated with associationPath '{}', appending join condition.", alias.getAlias(), alias.getAssociationPath());
                    existing.setJoinCondition(Restrictions.and(existing.getJoinCondition(), alias.getJoinCondition()));
                } else {
                    LOG.debug("alias '{}' already associated with associationPath '{}', adding join condition.", alias.getAlias(), alias.getAssociationPath());
                    existing.setJoinCondition(alias.getJoinCondition());
                }
            } else {
                LOG.debug("alias '{}' already associated with associationPath '{}', skipping.", alias.getAlias(), alias.getAssociationPath());
            }
        }
        return this;
    }

    public final Collection<Alias> getAliasCollection() {
        // make a copy so the internal one can't be modified outside of the builder
        return new ArrayList<Alias>(m_aliases.values());
    }

}
