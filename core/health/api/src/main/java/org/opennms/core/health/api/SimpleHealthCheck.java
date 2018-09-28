/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.health.api;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A very simple {@link HealthCheck} implementation, which may be used
 * if the health is okay, if a certain command ran sucessfully, e.g. instantiating an object, or similar.
 *
 * @author mvrueden
 */
public class SimpleHealthCheck implements HealthCheck {

    private final Supplier<String> descriptionSupplier;
    private Response response = new Response(Status.Starting);

    public SimpleHealthCheck(Supplier<String> descriptionSupplier) {
        this.descriptionSupplier = Objects.requireNonNull(descriptionSupplier);
    }

    @Override
    public String getDescription() {
        return descriptionSupplier.get();
    }

    @Override
    public Response perform() throws Exception {
        return response;
    }

    public void markSucess() {
        response = new Response(Status.Success);
    }

    public void markError(Exception e) {
        response = new Response(e);
    }
}
