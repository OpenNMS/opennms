/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.core.criteria.restrictions;

public interface RestrictionVisitor {
    default void visitNull(final NullRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitNullComplete(final NullRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitNotNull(final NotNullRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitNotNullComplete(final NotNullRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitEq(final EqRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitEqComplete(final EqRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitEqProperty(final EqPropertyRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitEqPropertyComplete(final EqPropertyRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitNe(final NeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitNeComplete(final NeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitGt(final GtRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitGtComplete(final GtRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitGe(final GeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitGeComplete(final GeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitLt(final LtRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitLtComplete(final LtRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitLe(final LeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitLeComplete(final LeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitAll(final AllRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitAllComplete(final AllRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitAny(final AnyRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitAnyComplete(final AnyRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitLike(final LikeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitLikeComplete(final LikeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitIlike(final IlikeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitIlikeComplete(final IlikeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitIn(final InRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitInComplete(final InRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitNot(final NotRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitNotComplete(final NotRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitBetween(final BetweenRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitBetweenComplete(final BetweenRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitSql(final SqlRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitSqlComplete(final SqlRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitIplike(final IplikeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitIplikeComplete(final IplikeRestriction restriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
    default void visitRegExp(RegExpRestriction regExpRestriction) {
        // default visitor does nothing, implementers can choose which are relevant
    }
}
