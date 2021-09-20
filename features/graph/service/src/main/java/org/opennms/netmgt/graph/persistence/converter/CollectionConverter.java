/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.persistence.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.persistence.converter.collection.SerializedCollection;
import org.opennms.netmgt.graph.persistence.converter.collection.SerializedCollectionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Transforms a {@link java.util.Collection} from and to a String to be persisted/serialized to the database. <br/>
 * The {@link java.util.Collection} may only contain elements of a type that is supported by the {@link ConverterService}.<br/>
 * The conversion follows the following pattern:<br/>
 * {@link java.util.Collection} <-> {@link SerializedCollection} <-> JSON String.
 * The {@link SerializedCollection} is the Java representation of the JSON object.<br/>
 * The elements in the Collection are converted to Strings using the {@link ConverterService}.
 */
public class CollectionConverter implements Converter<Collection<?>> {

    private Logger LOG = LoggerFactory.getLogger(CollectionConverter.class);

    private ConverterService converterService;
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(Class.class, new ClassAdapter())
            .create();

    CollectionConverter(ConverterService converterService) {
        this.converterService = converterService;
    }

    @Override
    public String toStringRepresentation(Collection<?> collection) {
        final List<SerializedCollectionEntry> persistedEntries = collection.stream()
                .map((entry) -> new SerializedCollectionEntry(entry.getClass(), converterService.toStringRepresentation(entry.getClass(), entry)))
                .collect(Collectors.toList());
        SerializedCollection serializedCollection = new SerializedCollection();
        serializedCollection.setType(collection.getClass());
        serializedCollection.setEntries(persistedEntries);
        return gson.toJson(serializedCollection);
    }

    @Override
    public Collection<?> toValue(Class<Collection<?>> type, String string) {
        final SerializedCollection serializedCollection = gson.fromJson(string, SerializedCollection.class);
        final ArrayList values = new ArrayList<>();
        serializedCollection.getEntries()
                .stream()
                .map(entry -> converterService.toValue(entry.getType(), entry.getValue()))
                .forEach(values::add);
        final Collection resurrectedCollection = recreateCollection(serializedCollection.getType(), values);
        return resurrectedCollection;
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    private Collection<?> recreateCollection(Class<Collection<?>> type, List values) {
        if (ImmutableList.class.isAssignableFrom(type)) {
            return ImmutableList.copyOf(values);
        } else if (ImmutableSet.class.isAssignableFrom(type)) {
            return ImmutableSet.copyOf(values);
        } else {
            LOG.warn("Cannot recreate {}, will use ImmutableList instead.", type);
            return ImmutableList.copyOf(values);
        }
    }

    /** We need this adapter so that the class object in SerializedCollection can be (de)serialized. */
    private final static class ClassAdapter implements JsonSerializer<Class>, JsonDeserializer<Class> {

        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }

        public Class deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Class.forName(json.getAsJsonPrimitive().getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
