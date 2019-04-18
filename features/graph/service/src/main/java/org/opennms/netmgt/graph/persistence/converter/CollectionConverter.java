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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.persistence.converter.collection.PersistedCollection;
import org.opennms.netmgt.graph.persistence.converter.collection.PersistedCollectionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Transforms a Collection from and to a String to be persisted in the database.<br>
 * The Collection may only contain elements of a type that is supported by the ConverterService.<br>
 * The conversation follows the following pattern:<br>
 * Collection <-> PersistedCollection <-> JsonString. The PersistedCollection is the Java representation of the Json object.<br>
 * The elements in the Collection are converted to Strings using the ConverterService.<br>
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
        List<PersistedCollectionEntry> persistedEntries = collection.stream()
                .map((entry) -> new PersistedCollectionEntry(entry.getClass(), converterService.toStringRepresentation(entry.getClass(), entry)))
                .collect(Collectors.toList());
        PersistedCollection persistedCollection = new PersistedCollection();
        persistedCollection.setType(collection.getClass());
        persistedCollection.setEntries(persistedEntries);
        return gson.toJson(persistedCollection);
    }

    @Override
    public Collection<?> toValue(Class<Collection<?>> type, String string) {

        Collection resurrectedCollection;

        PersistedCollection persistedCollection = gson.fromJson(string, PersistedCollection.class);
        resurrectedCollection = recreateCollection(persistedCollection.getType());

        persistedCollection.getEntries()
                .stream()
                .map(entry -> converterService.toValue(entry.getType(), entry.getValue()))
                .forEach(resurrectedCollection::add);

        return resurrectedCollection;
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    private Collection<?> recreateCollection(Class<Collection<?>> type) {
        Collection resurrectedCollection;
        try {
            resurrectedCollection = type.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOG.warn("Cannot recreate {}, will use ArrayList instead. Problem: {}", type, e.getMessage());
            resurrectedCollection = new ArrayList();
        }
        return resurrectedCollection;
    }

    /** We need this adapter so that the class object in PersistedCollection can be (de)serialized. */
    private final static class ClassAdapter implements JsonSerializer<Class>, JsonDeserializer<Class> {

        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }

        public Class deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return Class.forName(json.getAsJsonPrimitive().getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
