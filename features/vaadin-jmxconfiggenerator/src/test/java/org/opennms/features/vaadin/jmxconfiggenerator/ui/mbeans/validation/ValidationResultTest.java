/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import com.vaadin.data.Validator;
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
