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
	public void visitNull(final NullRestriction restriction);
	public void visitNullComplete(final NullRestriction restriction);
	public void visitNotNull(final NotNullRestriction restriction);
	public void visitNotNullComplete(final NotNullRestriction restriction);
    public void visitEq(final EqRestriction restriction);
    public void visitEqComplete(final EqRestriction restriction);
    public void visitNe(final NeRestriction restriction);
    public void visitNeComplete(final NeRestriction restriction);
	public void visitGt(final GtRestriction restriction);
	public void visitGtComplete(final GtRestriction restriction);
	public void visitGe(final GeRestriction restriction);
	public void visitGeComplete(final GeRestriction restriction);
	public void visitLt(final LtRestriction restriction);
	public void visitLtComplete(final LtRestriction restriction);
	public void visitLe(final LeRestriction restriction);
	public void visitLeComplete(final LeRestriction restriction);
	public void visitAll(final AllRestriction restriction);
	public void visitAllComplete(final AllRestriction restriction);
	public void visitAny(final AnyRestriction restriction);
	public void visitAnyComplete(final AnyRestriction restriction);
	public void visitLike(final LikeRestriction restriction);
	public void visitLikeComplete(final LikeRestriction restriction);
	public void visitIlike(final IlikeRestriction restriction);
	public void visitIlikeComplete(final IlikeRestriction restriction);
	public void visitIn(final InRestriction restriction);
	public void visitInComplete(final InRestriction restriction);
	public void visitNot(final NotRestriction restriction);
	public void visitNotComplete(final NotRestriction restriction);
	public void visitBetween(final BetweenRestriction restriction);
	public void visitBetweenComplete(final BetweenRestriction restriction);
	public void visitSql(final SqlRestriction restriction);
	public void visitSqlComplete(final SqlRestriction restriction);
	public void visitIplike(final IplikeRestriction restriction);
	public void visitIplikeComplete(final IplikeRestriction restriction);
}
