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
package org.opennms.core.health.api;

import java.util.ArrayList;
import java.util.List;
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
    public Response perform(Context context) throws Exception {
        return response;
    }

    @Override
    public List<String> getTags() {
        return new ArrayList<>();
    }

    public void markSucess() {
        response = new Response(Status.Success);
    }

    public void markError(Exception e) {
        response = new Response(e);
    }
}
