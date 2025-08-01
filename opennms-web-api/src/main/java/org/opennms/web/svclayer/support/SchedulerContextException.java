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
package org.opennms.web.svclayer.support;

import java.text.MessageFormat;
import java.util.Objects;

public class SchedulerContextException extends SchedulerException {

    private final String context;

    public SchedulerContextException(String context, String message) {
        super(Objects.requireNonNull(message));
        this.context = Objects.requireNonNull(context);
    }

    public SchedulerContextException(String context, String message, Exception ex) {
        super(message, ex);
        this.context = Objects.requireNonNull(context);
    }

    public SchedulerContextException(String context, String messageFormat, Object... arguments) {
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
