/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.bsm.model.meta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionDTO;
import org.reflections.Reflections;

import com.google.common.base.Throwables;

public class FunctionsManager {

    private List<Class<?>> getTypesAnnotatedWithFunction(String packageToScan) {
        Reflections reflections = new Reflections(packageToScan);
        Set<Class<?>> functions = reflections.getTypesAnnotatedWith(Function.class);
        return new ArrayList<>(functions);
    }

    public List<Class<?>> getMapFunctions() {
        return getTypesAnnotatedWithFunction("org.opennms.netmgt.bsm.service.model.functions.map");
    }

    public List<Class<?>> getReduceFunctions() {
        return getTypesAnnotatedWithFunction("org.opennms.netmgt.bsm.service.model.functions.reduce");
    }

    public FunctionMetaDTO getMapFunctionMetaData(String name) {
        Class<?> functionClass = findFunction(getMapFunctions(), name);
        if (functionClass != null) {
            return new FunctionMetaDTO(functionClass, FunctionType.MapFunction);
        }
        return null;
    }

    public FunctionMetaDTO getReduceFunctionMetaData(String name) {
        Class<?> functionClass = findFunction(getReduceFunctions(), name);
        if (functionClass != null) {
            return new FunctionMetaDTO(functionClass, FunctionType.ReduceFunction);
        }
        return null;
    }

    private Class<?> findFunction(List<Class<?>> functionMetaData, String name) {
        for (Class<? > eachClass : functionMetaData) {
            Function eachFunction = eachClass.getAnnotation(Function.class);
            if (name.equals(eachFunction.name())) {
                return eachClass;
            }
        }
        return null;
    }

    public MapFunctionDTO getMapFunctionDTO(MapFunction mapFunction) {
        Objects.requireNonNull(mapFunction);
        final FunctionMetaDTO functionMeta = new FunctionMetaDTO(mapFunction.getClass(), FunctionType.MapFunction);
        final MapFunctionDTO mapFunctionDTO = new MapFunctionDTO();
        mapFunctionDTO.setType(functionMeta.getName());
        mapFunctionDTO.setProperties(getFunctionProperties(mapFunction));
        return mapFunctionDTO;
    }

    public ReduceFunctionDTO getReduceFunctionDTO(ReductionFunction reduceFunction) {
        Objects.requireNonNull(reduceFunction);
        final FunctionMetaDTO functionMeta = new FunctionMetaDTO(reduceFunction.getClass(), FunctionType.ReduceFunction);
        final ReduceFunctionDTO reduceFunctionDTO = new ReduceFunctionDTO();
        reduceFunctionDTO.setType(functionMeta.getName());
        reduceFunctionDTO.setProperties(getFunctionProperties(reduceFunction));
        return reduceFunctionDTO;

    }

    public Map<String, String> getFunctionProperties(MapFunction mapFunction) {
        return getParametersAsProperties(mapFunction);
    }

    public Map<String, String> getFunctionProperties(ReductionFunction reduceFunction) {
        return getParametersAsProperties(reduceFunction);
    }

    private <T> Map<String, String> getParametersAsProperties(T function) {
        Map<String, String> propertiesMap = new HashMap<>();
        for (Field eachField : function.getClass().getDeclaredFields()) {
            Parameter parameter = eachField.getAnnotation(Parameter.class);
            if (parameter != null) {
                try {
                    eachField.setAccessible(true);
                    Object value = eachField.get(function);
                    propertiesMap.put(parameter.key(), String.valueOf(value));
                } catch (IllegalAccessException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
        return propertiesMap;
    }

    public MapFunction getMapFunction(MapFunctionDTO input) {
        Objects.requireNonNull(input);
        @SuppressWarnings("unchecked")
        Class<? extends MapFunction> functionClass = (Class<? extends MapFunction>) findFunction(getMapFunctions(), input.getType());
        return createFunctionInstance(functionClass, input.getProperties());
    }

    public <T> T createFunctionInstance(Class<? extends T> functionClass, Map<String, String> parameterMap) {
        Objects.requireNonNull(functionClass);
        T functionInstance;
        try {
            functionInstance = functionClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw Throwables.propagate(e);
        }
        // TODO this logic is very similar to what AbstractFilterFactory (Measurement API) does
        for(Field field : functionClass.getDeclaredFields()) {
            Parameter parameter = field.getAnnotation(Parameter.class);
            if (parameter != null) {
                String effectiveValueAsStr;
                if (parameterMap.containsKey(parameter.key())) {
                    effectiveValueAsStr = parameterMap.get(parameter.key());
                } else if (!parameter.required()) {
                    effectiveValueAsStr = parameter.defaultValue();
                } else {
                    throw new IllegalArgumentException("Parameter with key '" + parameter.key() + "' is required, but no value was given.");
                }

                // Convert the value to the appropriate type
                Object effectiveValue = effectiveValueAsStr;
                if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                    effectiveValue = Boolean.valueOf(effectiveValueAsStr);
                } else if (field.getType() == Double.class || field.getType() == double.class) {
                    effectiveValue = Double.valueOf(effectiveValueAsStr);
                } else if (field.getType() == Float.class || field.getType() == float.class) {
                    effectiveValue = Float.valueOf(effectiveValueAsStr);
                } else if (field.getType() == Integer.class || field.getType() == int.class) {
                    effectiveValue = Integer.valueOf(effectiveValueAsStr);
                } else if (field.getType() == Long.class || field.getType() == long.class) {
                    effectiveValue = Long.valueOf(effectiveValueAsStr);
                } else if (field.getType().isEnum()) {
                    // we manually find the correct enum constant, as we require case insensitive match
                    Object[] enumConstants = field.getType().getEnumConstants();
                    for (Object eachEnumConstant : enumConstants) {
                        if (effectiveValueAsStr.equalsIgnoreCase(String.valueOf(eachEnumConstant))) {
                            effectiveValue = eachEnumConstant;
                            break;
                        }
                    }
                }

                // Set the field's value
                try {
                    field.setAccessible(true);
                    field.set(functionInstance, effectiveValue);
                } catch (ReflectiveOperationException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
        return functionInstance;
    }

    public ReductionFunction getReduceFunction(ReduceFunctionDTO input) {
        Objects.requireNonNull(input);
        @SuppressWarnings("unchecked")
        Class<? extends ReductionFunction> functionClass = (Class<? extends ReductionFunction>) findFunction(getReduceFunctions(), input.getType());
        return createFunctionInstance(functionClass, input.getProperties());
    }
}
