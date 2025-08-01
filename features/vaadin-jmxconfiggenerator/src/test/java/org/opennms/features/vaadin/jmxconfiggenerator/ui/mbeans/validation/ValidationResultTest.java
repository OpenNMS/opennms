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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import com.vaadin.v7.data.Validator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ValidationResultTest {

    @Test
    public void testGetErrorCount() {
        ValidationResult result = new ValidationResult();

        Assert.assertEquals(0, result.getErrorCount());

        result.add("A", createDummyException(10));
        result.add("A", createDummyException(11));
        result.add("B", createDummyException(20));
        result.add("B", createDummyException(21));

        Assert.assertEquals(4, result.getErrorCount());
    }

    @Test
    public void testGetValidationErrors() {
        ValidationResult result = new ValidationResult();

        Assert.assertNotNull(result.getValidationErrors());
        Assert.assertEquals(0, result.getValidationErrors().size());

        result.add("A", createDummyException(10));
        result.add(100, createDummyException(20));
        result.add("B", createDummyException(30));

        List<ValidationResult.ValidationError> errorList = result.getValidationErrors();
        Collections.sort(errorList, new Comparator<ValidationResult.ValidationError>() {
            @Override
            public int compare(ValidationResult.ValidationError o1, ValidationResult.ValidationError o2) {
                return o1.getErrorObject().toString().compareTo(o2.getErrorObject().toString());
            }
        });

        Assert.assertEquals(3, result.getValidationErrors().size());
        Assert.assertEquals(100, errorList.get(0).getErrorObject());
        Assert.assertEquals(1, errorList.get(0).getExceptionList().size());
        Assert.assertEquals("A", errorList.get(1).getErrorObject());
        Assert.assertEquals(1, errorList.get(1).getExceptionList().size());
        Assert.assertEquals("B", errorList.get(2).getErrorObject());
        Assert.assertEquals(1, errorList.get(2).getExceptionList().size());
    }

    @Test
    public void testGetValidationError() {
        ValidationResult result = new ValidationResult();

        result.add("A", createDummyException(10));
        result.add("A", createDummyException(11));
        result.add("B", createDummyException(20));
        result.add("B", createDummyException(21));
        result.add("C", createDummyException(30));
        result.add(300, createDummyException(40));

        Assert.assertEquals(6, result.getErrorCount());
        Assert.assertEquals(4, result.getValidationErrors().size());

        Assert.assertEquals(1, result.getValidationError("C").getExceptionList().size());
        Assert.assertEquals("C", result.getValidationError("C").getErrorObject());

        Assert.assertEquals(2, result.getValidationError("A").getExceptionList().size());
        Assert.assertEquals(0, result.getValidationError(200).getExceptionList().size());

        Assert.assertEquals(3, result.getValidationErrors(String.class).size());
        Assert.assertEquals(1, result.getValidationErrors(Integer.class).size());
    }

    @Test
    public void testMerge() {
        ValidationResult result = new ValidationResult();

        ValidationResult result2 = new ValidationResult();
        result2.add("A", createDummyException(1));

        result.merge(result2);
        Assert.assertEquals(1, result.getErrorCount());
        Assert.assertEquals("A", result.getValidationError("A").getErrorObject());
    }

    private static Validator.InvalidValueException createDummyException(int number) {
        return new Validator.InvalidValueException(createDummyErrorMessage(number));
    }

    private static String createDummyErrorMessage(int number) {
        return String.format("Dummy Error Message %d", number);
    }


}
