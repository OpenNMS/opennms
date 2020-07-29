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

package org.opennms.doctests.model;

import java.util.Optional;

import org.opennms.smoketest.stacks.OpenNMSProfile;

import com.google.common.base.MoreObjects;

public abstract class Preparation extends Snippet {

    protected Preparation(final Builder<?> builder) {
        super(builder);
    }

    public abstract void prepare(final Sequence sequence,
                                 final OpenNMSProfile.Builder profile) throws Exception;

    @Override
    public Optional<Execution> asExecution() {
        return Optional.empty();
    }

    @Override
    public Optional<Preparation> asPreparation() {
        return Optional.of(this);
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper();
    }

    public static abstract class Builder<R extends Builder<R>> extends Snippet.Builder<R> {
        @Override
        public abstract Preparation build();
    }
}
