/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private final String m_criteriaPropertyName;
    private final BiConsumer<CriteriaBuilder,Object> m_beforeVisit;
    private final Function<String,T> m_converter;

    public CriteriaBehavior(String name) {
        this(name, null, (b,v)-> {});
    }

    public CriteriaBehavior(Function<String,T> converter) {
        this(null, converter, (b,v) -> {});
    }

    public CriteriaBehavior(String name, BiConsumer<CriteriaBuilder,Object> beforeVisit) {
        this(name, null, beforeVisit);
    }

    public CriteriaBehavior(String name, Function<String,T> converter) {
        this(name, converter, (b,v) -> {});
    }

    public CriteriaBehavior(String name, Function<String,T> converter, BiConsumer<CriteriaBuilder,Object> beforeVisit) {
        m_criteriaPropertyName = name;
        m_converter = converter;
        m_beforeVisit = beforeVisit;
    }

    public String getPropertyName() {
        return m_criteriaPropertyName;
    }

    public void beforeVisit(CriteriaBuilder builder, Object value) {
        m_beforeVisit.accept(builder, value);
    }

    public T convert(String value) {
        return m_converter.apply(value);
    }
}