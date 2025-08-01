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
