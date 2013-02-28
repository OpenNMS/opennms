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

package org.opennms.core.criteria.restrictions;

public interface RestrictionVisitor {
    void visitNull(final NullRestriction restriction);
    void visitNullComplete(final NullRestriction restriction);
    void visitNotNull(final NotNullRestriction restriction);
    void visitNotNullComplete(final NotNullRestriction restriction);
    void visitEq(final EqRestriction restriction);
    void visitEqComplete(final EqRestriction restriction);
    void visitNe(final NeRestriction restriction);
    void visitNeComplete(final NeRestriction restriction);
    void visitGt(final GtRestriction restriction);
    void visitGtComplete(final GtRestriction restriction);
    void visitGe(final GeRestriction restriction);
    void visitGeComplete(final GeRestriction restriction);
    void visitLt(final LtRestriction restriction);
    void visitLtComplete(final LtRestriction restriction);
    void visitLe(final LeRestriction restriction);
    void visitLeComplete(final LeRestriction restriction);
    void visitAll(final AllRestriction restriction);
    void visitAllComplete(final AllRestriction restriction);
    void visitAny(final AnyRestriction restriction);
    void visitAnyComplete(final AnyRestriction restriction);
    void visitLike(final LikeRestriction restriction);
    void visitLikeComplete(final LikeRestriction restriction);
    void visitIlike(final IlikeRestriction restriction);
    void visitIlikeComplete(final IlikeRestriction restriction);
    void visitIn(final InRestriction restriction);
    void visitInComplete(final InRestriction restriction);
    void visitNot(final NotRestriction restriction);
    void visitNotComplete(final NotRestriction restriction);
    void visitBetween(final BetweenRestriction restriction);
    void visitBetweenComplete(final BetweenRestriction restriction);
    void visitSql(final SqlRestriction restriction);
    void visitSqlComplete(final SqlRestriction restriction);
    void visitIplike(final IplikeRestriction restriction);
    void visitIplikeComplete(final IplikeRestriction restriction);
}
