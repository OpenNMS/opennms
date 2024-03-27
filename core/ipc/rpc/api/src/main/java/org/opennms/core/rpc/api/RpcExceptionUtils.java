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
package org.opennms.core.rpc.api;

import java.util.Objects;
import java.util.Optional;

public class RpcExceptionUtils {

    public static <T> T handleException(Throwable t, RpcExceptionHandler<T> visitor) {
        Objects.requireNonNull(t);
        Objects.requireNonNull(visitor);

        // Attempt to handle the exception directly
        Optional<T> val = handleExceptionInternal(t, visitor);
        if (val != null) {
            return val.orElse(null);
        }

        // No match was made, attempt to handle the cause if present
        final Throwable cause = t.getCause();
        if (cause != null) {
            val = handleExceptionInternal(cause, visitor);
            if (val != null) {
                return val.orElse(null);
            }
        }

        // No match for parent or cause
        return visitor.onUnknown(t);
    }

    private static <T> Optional<T> handleExceptionInternal(Throwable t, RpcExceptionHandler<T> visitor) {
        if (t instanceof InterruptedException) {
            return Optional.ofNullable(visitor.onInterrupted(t));
        } else if (t instanceof RequestRejectedException) {
            return Optional.ofNullable(visitor.onRejected(t));
        } else if (t instanceof java.util.concurrent.RejectedExecutionException) {
            return Optional.ofNullable(visitor.onRejected(t));
        } else if (t instanceof RequestTimedOutException) {
            return Optional.ofNullable(visitor.onTimedOut(t));
        } else {
            return null;
        }
    }
}
