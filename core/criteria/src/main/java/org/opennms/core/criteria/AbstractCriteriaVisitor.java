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

import org.opennms.core.criteria.Criteria.CriteriaVisitor;
import org.opennms.core.criteria.restrictions.Restriction;

public class AbstractCriteriaVisitor implements CriteriaVisitor {

    @Override
    public void visitClass(final Class<?> clazz) {
    }

    @Override
    public void visitOrder(final Order order) {
    }

    @Override
    public void visitOrdersFinished() {
    }

    @Override
    public void visitAlias(final Alias alias) {
    }

    @Override
    public void visitAliasesFinished() {
    }

    @Override
    public void visitFetch(final Fetch fetch) {
    }

    @Override
    public void visitFetchesFinished() {
    }

    @Override
    public void visitRestriction(final Restriction restriction) {
    }

    @Override
    public void visitRestrictionsFinished() {
    }

    @Override
    public void visitDistinct(final boolean distinct) {
    }

    @Override
    public void visitLimit(final Integer limit) {
    }

    @Override
    public void visitOffset(final Integer offset) {
    }

}
