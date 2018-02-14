/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import java.util.function.Function;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;

/**
 * Specific {@link CriteriaBehavior} for IP address values that can handle
 * {@code iplike} String values.
 * 
 * NOTE: Because {@link CriteriaBuilder#iplike(String, Object)} uses
 * {@link Restrictions#iplike(String, Object)} and it uses
 * {@link org.hibernate.criterion.Restrictions#sqlRestriction(String)},
 * this behavior can only be used against the root alias. If we enhanced
 * the Criteria API to expose subcriteria for aliases than it might
 * be possible to leverage iplike against aliased properties.
 */
public class IpLikeCriteriaBehavior extends CriteriaBehavior<String> {

    public IpLikeCriteriaBehavior(String name) {
        this(name, (b,v,c,w)-> {
            switch(c) {
            case EQUALS:
                // Undo the FIQL wildcard replacements
                b.iplike(name, ((String)v).replaceAll("%", "*"));
                break;
            case NOT_EQUALS:
                // Undo the FIQL wildcard replacements
                b.not().iplike(name, ((String)v).replaceAll("%", "*"));
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type for iplike expression: " + c.toString());
            }
        });
    }

    protected IpLikeCriteriaBehavior(String name, BeforeVisit beforeVisit) {
        super(name, Function.identity(), beforeVisit);
        // Skip normal processing for this property since we're applying
        // the filter in the BeforeVisit operation.
        super.setSkipPropertyByDefault(true);
    }
}
