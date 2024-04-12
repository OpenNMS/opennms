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
package org.opennms.core.mate.api.parser;

import java.util.List;

public interface Expression {
    void accept(final Visitor visitor);

    interface Visitor {
        default void start() {
        }

        default void finish() {
        }

        default void visit(final PlainTextExpression plainTextExpression) {
        }

        default void visit(final JexlExpression jexlExpression) {
        }

        default void visit(final SimpleExpression simpleExpression) {
        }
    }

    static <V extends Visitor> V visit(final List<Expression> elements, final V visitor) {
        visitor.start();

        for (final Expression element : elements) {
            element.accept(visitor);
        }

        visitor.finish();

        return visitor;
    }
}
