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
package org.opennms.netmgt.flows.classification.error;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;

// Generic error object to handle user friendly error messages
public class Error {

    // Additional (optional) context of the error (e.g. an attribute name)
    private final String context;

    private final ErrorTemplate template;

    // Arguments to be used in the message format.
    private final Object[] arguments;

    public Error(String context, ErrorTemplate errorTemplate, Object... arguments) {
        this.context = context;
        this.template = Objects.requireNonNull(errorTemplate);
        this.arguments = arguments;
    }

    private Object[] getArguments() {
        return arguments;
    }

    public String getFormattedMessage() {
        return new MessageFormat(template.getMessage()).format(getArguments());
    }

    public ErrorTemplate getTemplate() {
        return template;
    }

    public String getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Error error = (Error) o;
        return Objects.equals(template, error.template)
                && Arrays.equals(arguments, error.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template, arguments);
    }
}
