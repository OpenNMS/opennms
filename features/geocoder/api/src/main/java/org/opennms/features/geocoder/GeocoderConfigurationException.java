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
package org.opennms.features.geocoder;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * In case a {@link GeocoderService} is not configured properly, it should throw this exception.
 *
 * @author mvrueden
 */
public class GeocoderConfigurationException extends RuntimeException {
    private final String context;

    public GeocoderConfigurationException(String context, String message) {
        super(Objects.requireNonNull(message));
        this.context = Objects.requireNonNull(context);
    }

    public GeocoderConfigurationException(String context, String messageFormat, Object... arguments) {
        this(context, new MessageFormat(messageFormat).format(arguments));
    }

    @Override
    public String getMessage() {
        return this.context + ":" + super.getMessage();
    }

    public String getContext() {
        return context;
    }

    public String getRawMessage() {
        return super.getMessage();
    }
}
