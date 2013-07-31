/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.criteria.Alias.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliasBuilder {
	
	private static final Logger LOG = LoggerFactory.getLogger(AliasBuilder.class);
	
    final Map<String, Alias> m_aliases = new HashMap<String, Alias>();

    public AliasBuilder alias(final String associationPath, final String alias, final JoinType type) {
        if (m_aliases.containsKey(alias)) {
        	LOG.debug("alias '{}' already associated with associationPath '{}', skipping.", alias, associationPath);
        } else {
            m_aliases.put(alias, new Alias(associationPath, alias, type));
        }
        return this;
    }

    public Collection<Alias> getAliasCollection() {
        // make a copy so the internal one can't be modified outside of the
        // builder
        return new ArrayList<Alias>(m_aliases.values());
    }

}
