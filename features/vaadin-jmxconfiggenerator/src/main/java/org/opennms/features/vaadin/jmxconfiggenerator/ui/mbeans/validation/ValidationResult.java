/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.vaadin.data.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValidationResult {

    public static class ValidationError {

        private final List<Validator.InvalidValueException> exceptionList;
        private final Object errorObject;

        public ValidationError(Object errorObject, Set<Validator.InvalidValueException> exceptionList) {
            this.errorObject = errorObject;
            this.exceptionList = new ArrayList<>(exceptionList);
        }

        public Object getErrorObject() {
            return errorObject;
        }

        public List<Validator.InvalidValueException> getExceptionList() {
            return exceptionList;
        }
    }

    private final Map<Object, Set<Validator.InvalidValueException>> errorMap = new HashMap<>();

    public <T> void add(T itemId, Validator.InvalidValueException validationException) {
        if (errorMap.get(itemId) == null) {
            errorMap.put(itemId, new HashSet<Validator.InvalidValueException>());
        }
        errorMap.get(itemId).add(validationException);
    }

    public void merge(ValidationResult resultToMerge) {
        if (resultToMerge != null && !resultToMerge.isValid()) {
            for (Map.Entry<Object, Set<Validator.InvalidValueException>> eachEntry : resultToMerge.errorMap.entrySet()) {
                if (!errorMap.containsKey(eachEntry.getKey())) {
                    errorMap.put(eachEntry.getKey(), eachEntry.getValue());
                } else {
                    errorMap.get(eachEntry.getKey()).addAll(eachEntry.getValue());
                }
            }
        }
    }

    public boolean isValid() {
        return errorMap.isEmpty();
    }

    protected <T> ValidationError getValidationError(final T object) {
        Set<Validator.InvalidValueException> exceptionList = errorMap.get(object);
        exceptionList = exceptionList == null ? Collections.<Validator.InvalidValueException>emptySet() : exceptionList;
        return new ValidationError(object, exceptionList);
    }

    public Collection<ValidationError> getValidationErrors(final Class<?> clazz) {
        return Collections2.filter(getValidationErrors(), new Predicate<ValidationError>() {
            @Override
            public boolean apply(ValidationError input) {
                return input.errorObject.getClass() == clazz;
            }
        });
    }

    public List<ValidationError> getValidationErrors() {
        List<ValidationError> errorList = new ArrayList<>();
        for (Map.Entry<Object, Set<Validator.InvalidValueException>> eachEntry : errorMap.entrySet()) {
            errorList.add(new ValidationError(eachEntry.getKey(), eachEntry.getValue()));

        }
        return errorList;
    }

    public int getErrorCount() {
        int counter = 0;
        for (Set<Validator.InvalidValueException> values : errorMap.values()) {
            counter += values.size();
        }
        return counter;
    }
}
