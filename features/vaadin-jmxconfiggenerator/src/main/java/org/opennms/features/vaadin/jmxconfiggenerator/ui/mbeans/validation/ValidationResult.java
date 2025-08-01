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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.vaadin.v7.data.Validator;

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
