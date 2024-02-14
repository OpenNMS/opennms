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
package org.opennms.web.rest.support;

import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.opennms.core.criteria.CriteriaBuilder;

/**
 * <p>Specify a custom handler for a FIQL query term. This allows us to:</p>
 * <ul>
 * <li>Map the query term to a different criteria property name</li>
 * <li>Specify a function to convert the String value to a specific Java type (Enum, Integer, etc.)</li>
 * <li>Specify a function that will be executed when the search term is specified</li>
 * </ul>
 * 
 * @param <T>
 */
public class CriteriaBehavior<T> {

    @FunctionalInterface
    public interface BeforeVisit {
        /**
         * Interface that specifies the action to take for a given query term.
         * 
         * @param b {@link CriteriaBuilder} that is being used to construct the query
         * @param v The value of the query term
         * @param c The condition type of the query term
         * @param w Boolean indicating whether the term is a wildcard (that should 
         *   use {@code LIKE} instead of {@code =} in a query, for instance)
         */
        public void accept(CriteriaBuilder b, Object v, ConditionType c, boolean w);
    }

    private final String m_criteriaPropertyName;
    private final BeforeVisit m_beforeVisit;
    private final Function<String,T> m_converter;
    private boolean m_skipProperty = false;

    public CriteriaBehavior(Function<String,T> converter) {
        this(null, converter, (b,v,c,w) -> {});
    }

    public CriteriaBehavior(String name, Function<String,T> converter) {
        this(name, converter, (b,v,c,w) -> {});
    }

    public CriteriaBehavior(String name, Function<String,T> converter, BeforeVisit beforeVisit) {
        m_criteriaPropertyName = name;
        m_converter = converter;
        m_beforeVisit = beforeVisit;
    }

    public String getPropertyName() {
        return m_criteriaPropertyName;
    }

    public Function<String,T> getConverter() {
        return m_converter;
    }

    public void beforeVisit(CriteriaBuilder builder, Object value, ConditionType c, boolean isWildcard) {
        m_beforeVisit.accept(builder, value, c, isWildcard);
    }

    public T convert(String value) {
        return m_converter.apply(value);
    }

    public void setSkipPropertyByDefault(boolean skip) {
        m_skipProperty = skip;
    }

    public boolean shouldSkipProperty(ConditionType condition, boolean wildcard) {
        return m_skipProperty;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("criteriaPropertyName", m_criteriaPropertyName)
            .append("converter", m_converter)
            .append("beforeVisit", m_beforeVisit)
            .append("skipProperty", m_skipProperty)
            .build();
    }
}
