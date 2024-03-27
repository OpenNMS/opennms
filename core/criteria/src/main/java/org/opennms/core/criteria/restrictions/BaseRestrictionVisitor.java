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
package org.opennms.core.criteria.restrictions;

public class BaseRestrictionVisitor implements RestrictionVisitor {

    @Override public void visitNull(final NullRestriction restriction) {}
    @Override public void visitNullComplete(final NullRestriction restriction) {}
    @Override public void visitNotNull(final NotNullRestriction restriction) {}
    @Override public void visitNotNullComplete(final NotNullRestriction restriction) {}
    @Override public void visitEq(final EqRestriction restriction) {}
    @Override public void visitEqComplete(final EqRestriction restriction) {}
    @Override public void visitEqProperty(final EqPropertyRestriction restriction) {}
    @Override public void visitEqPropertyComplete(final EqPropertyRestriction restriction) {}
    @Override public void visitNe(final NeRestriction restriction) {}
    @Override public void visitNeComplete(final NeRestriction restriction) {}
    @Override public void visitGt(final GtRestriction restriction) {}
    @Override public void visitGtComplete(final GtRestriction restriction) {}
    @Override public void visitGe(final GeRestriction restriction) {}
    @Override public void visitGeComplete(final GeRestriction restriction) {}
    @Override public void visitLt(final LtRestriction restriction) {}
    @Override public void visitLtComplete(final LtRestriction restriction) {}
    @Override public void visitLe(final LeRestriction restriction) {}
    @Override public void visitLeComplete(final LeRestriction restriction) {}
    @Override public void visitAll(final AllRestriction restriction) {}
    @Override public void visitAllComplete(final AllRestriction restriction) {}
    @Override public void visitAny(final AnyRestriction restriction) {}
    @Override public void visitAnyComplete(final AnyRestriction restriction) {}
    @Override public void visitLike(final LikeRestriction restriction) {}
    @Override public void visitLikeComplete(final LikeRestriction restriction) {}
    @Override public void visitIlike(final IlikeRestriction restriction) {}
    @Override public void visitIlikeComplete(final IlikeRestriction restriction) {}
    @Override public void visitIn(final InRestriction restriction) {}
    @Override public void visitInComplete(final InRestriction restriction) {}
    @Override public void visitNot(final NotRestriction restriction) {}
    @Override public void visitNotComplete(final NotRestriction restriction) {}
    @Override public void visitBetween(final BetweenRestriction restriction) {}
    @Override public void visitBetweenComplete(final BetweenRestriction restriction) {}
    @Override public void visitSql(final SqlRestriction restriction) {}
    @Override public void visitSqlComplete(final SqlRestriction restriction) {}
    @Override public void visitIplike(final IplikeRestriction restriction) {}
    @Override public void visitIplikeComplete(final IplikeRestriction restriction) {}
    @Override public void visitRegExp(final RegExpRestriction restriction) {}
}
