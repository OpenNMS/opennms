/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow.v5.fields;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class AbstractField<T> {

    private final ByteBuffer data;
    private final int offset;
    private final int startIndex;
    private final int endIndex;
    private T value;

    public AbstractField(int startIndex, int endIndex, ByteBuffer data, int offset) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.data = data;
        this.offset = offset;

        if (startIndex < 0) {
            throw new IllegalArgumentException("startIndex must be >= 0");
        }
        if (endIndex >= data.array().length) {
            throw new IllegalArgumentException("endIndex must be < data.array().length");
        }
        if (startIndex > endIndex) {
            throw new IllegalArgumentException("startIndex must be < endIndex");
        }
    }

    public T getValue() {
        if (this.value == null) {
            this.value = extractValue();
        }
        return this.value;
    }

    protected byte[] getFieldBytes() {
        return Arrays.copyOfRange(data.array(), this.offset +  this.startIndex, this.offset + this.endIndex + 1);
    }

    protected abstract T extractValue();
}
