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
