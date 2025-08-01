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
package org.opennms.core.tasks;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This interface is used as a completion handler and exception
 * handler for {@link Async} operations.
 *
 * @author brozow
 * @version $Id: $
 */
public interface Callback<T> extends Consumer<T>, Function<Throwable,T> {

    /**
     * Use the {@link #handleException(Throwable)} as an alias for the
     * functional {@link #apply(Object)} method.
     *
     * @param t a {@link java.lang.Throwable} object.
     */
    default void handleException(Throwable t) {
        apply(t);
    }
}
