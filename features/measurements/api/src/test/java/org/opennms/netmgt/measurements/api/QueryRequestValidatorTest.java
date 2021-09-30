/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
import org.opennms.netmgt.measurements.model.Expression;

public class QueryRequestValidatorTest {

    @Test
    public void attributesMatcherShouldMatchCorrectly() {
        QueryRequestValidator v = new QueryRequestValidator();
        assertTrue(v.isValidAttributeName("a"));
        assertTrue(v.isValidAttributeName("ab"));
        assertTrue(v.isValidAttributeName("a3"));
        assertTrue(v.isValidAttributeName("_"));
        assertTrue(v.isValidAttributeName("$"));
        assertFalse(v.isValidAttributeName("3a"));
        assertFalse(v.isValidAttributeName("a-b"));
        assertFalse(v.isValidAttributeName("%"));
        assertFalse(v.isValidAttributeName("*"));
        assertFalse(v.isValidAttributeName("="));
        assertFalse(v.isValidAttributeName("!"));
        assertFalse(v.isValidAttributeName(""));
    }

    @Test
    public void shouldReportInvalidAttributes() {
        QueryRequestValidator v = new QueryRequestValidator();
        try {
            v.checkIfInvalidVariablesAreUsedInExpressions(Arrays.asList("CPO", "3CPO"), asListOfExpressions("Hello Yoda!", "Hello 3CPO!"));
            fail("expected ValidationException");
        } catch (ValidationException e) {
            // all good, we expect the exception
        }
    }

    @Test
    public void shouldAllowValidAttributes() throws ValidationException {
        QueryRequestValidator v = new QueryRequestValidator();
        v.checkIfInvalidVariablesAreUsedInExpressions(Arrays.asList("3CPO", "CPO"), asListOfExpressions("Hello Yoda!, Hello CPO!"));
    }

    private List<Expression> asListOfExpressions(String...expressions) {
        List<Expression> list = new ArrayList<>();
        for (String expression : expressions) {
            list.add(new Expression(UUID.randomUUID().toString(), expression, false));
        }
        return list;
    }

}
