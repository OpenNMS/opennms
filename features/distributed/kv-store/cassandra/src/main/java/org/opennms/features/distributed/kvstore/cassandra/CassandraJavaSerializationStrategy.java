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

package org.opennms.features.distributed.kvstore.cassandra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.opennms.features.distributed.kvstore.api.SerializationStrategy;

/**
 * A strategy for serializing/deserializing to a Cassandra 'blob' column using vanilla Java serialization.
 */
public enum CassandraJavaSerializationStrategy implements SerializationStrategy<ByteBuffer, Serializable> {
    INSTANCE;

    @Override
    public ByteBuffer serialize(Serializable rawValue) {
        ByteBuffer serializedValue;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(rawValue);
            out.flush();
            serializedValue = ByteBuffer.wrap(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return serializedValue;
    }

    @Override
    public Serializable deserialize(ByteBuffer serializedValue) {
        Serializable value;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedValue.array())) {
            ObjectInput in = new ObjectInputStream(bis);
            value = (Serializable) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return value;
    }
}
