/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Allows to select values from GSON documents
 *
 * The {@link #eval} method returns the selection result given a root JsonElement.
 *
 * GPath instances can be built either
 *
 * <ol>
 * <li>by starting from the last select step and adding the preceding selection steps
 * in a fluent interface like manner (e.g. the GPath {@code string().field("name")} accesses the
 * "name" field of a GSON document) or</li>
 * <li>by using nested methods calls (e.g. the nested method calls {@code field("name", string())} construct
 * the same GSON path).</li>
 * </ol>
 *
 * @param <O> the selection result
 */
public class GPath<O> {

    public static GPath<String> string() {
        return new GPath<String>(JsonElement::getAsString);
    }

    public static GPath<Double> dbl() {
        return new GPath<Double>(JsonElement::getAsDouble);
    }

    public static GPath<Integer> integer() {
        return new GPath<Integer>(JsonElement::getAsInt);
    }

    public static GPath<JsonElement> any() {
        return new GPath<JsonElement>(je -> je);
    }

    public static GPath<JsonObject> object() {
        return new GPath<JsonObject>(JsonElement::getAsJsonObject);
    }

    public static GPath<JsonArray> array() {
        return new GPath<JsonArray>(JsonElement::getAsJsonArray);
    }

    public static <X> GPath<X> field(String name, GPath<X> path) {
        return path.field(name);
    }

    public static <X> GPath<X> field(String name, String altName, GPath<X> path) {
        return path.field(name, altName);
    }

    public static <X> GPath<List<X>> array(String name, GPath<X> path) {
        return path.array(name);
    }

    private final Function<JsonElement, O> eval;

    public GPath(Function<JsonElement, O> eval) {
        this.eval = eval;
    }

    public GPath<O> field(String name) {
        return field(name, name);
    }

    public GPath<O> field(String name, String altName) {

        return new GPath<O>(je -> {
            if (je.isJsonObject()) {
                JsonObject jo = je.getAsJsonObject();
                JsonElement j = jo.get(name);
                JsonElement j2 = j != null ? j : jo.get(altName);
                if (j2 != null) {
                    return eval(j2);
                }
            }
            return null;
        });
    }

    public GPath<List<O>> array(String name) {
        return new GPath<List<O>>(je -> {
            if (je.isJsonObject()) {
                JsonElement j = je.getAsJsonObject().get(name);
                if (j != null && j.isJsonArray()) {
                    JsonArray array = j.getAsJsonArray();
                    if (array != null) {
                        ArrayList<O> list = new ArrayList<>();
                        array.forEach(i -> list.add(eval(i)));
                        return list;
                    }
                }
            }
            return null;
        });
    }

    public <X> GPath<X> map(Function<O, X> f) {
        return new GPath<X>(je -> {
            O o = eval(je);
            return o != null ? f.apply(o) : null;
        });
    }

    /**
     * Returns the selection result given the root JsonElement.
     *
     * @param in the root element the evaluation is base on.
     * @return in case that the GPath selects non-existing fields {@code null} is returned.
     */
    public O eval(JsonElement in) {
        return eval.apply(in);
    }

}
