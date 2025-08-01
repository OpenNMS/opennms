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

public interface RestrictionVisitor {
    void visitNull(final NullRestriction restriction);
    void visitNullComplete(final NullRestriction restriction);
    void visitNotNull(final NotNullRestriction restriction);
    void visitNotNullComplete(final NotNullRestriction restriction);
    void visitEq(final EqRestriction restriction);
    void visitEqComplete(final EqRestriction restriction);
    void visitEqProperty(final EqPropertyRestriction restriction);
    void visitEqPropertyComplete(final EqPropertyRestriction restriction);
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
    void visitRegExp(RegExpRestriction regExpRestriction);
}
