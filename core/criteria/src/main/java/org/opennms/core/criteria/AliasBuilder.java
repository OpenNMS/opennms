/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
