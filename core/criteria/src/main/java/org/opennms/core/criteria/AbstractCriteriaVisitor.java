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

import org.opennms.core.criteria.Criteria.CriteriaVisitor;
import org.opennms.core.criteria.Criteria.LockType;
import org.opennms.core.criteria.restrictions.Restriction;

public class AbstractCriteriaVisitor implements CriteriaVisitor {

    @Override
    public void visitClassAndRootAlias(final Class<?> clazz, final String rootAlias) {
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
    public void visitLockType(final LockType lock) {
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
