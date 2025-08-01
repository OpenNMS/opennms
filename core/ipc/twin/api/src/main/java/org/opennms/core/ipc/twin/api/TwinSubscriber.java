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
package org.opennms.core.ipc.twin.api;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * TwinSubscriber lives on Minion that handles all the Object subscriptions on Minion.
 * At boot up, a module that lives on Minion issues an RPC from Minion to OpenNMS to get an object.
 * Subsequent updates of the object will come as reverse sink messages from OpenNMS to Minion
 */
public interface TwinSubscriber extends Closeable {

    /**
     * @param key      Unique key for the object.
     * @param clazz    Specific bean class of T to marshal/unmarshal.
     * @param consumer Consumer of T for subsequent updates to T.
     * @param <T>      T is an object type that needs to be replicated from OpenNMS to Minion.
     * @return Closeable to close the subscription of T.
     */
    <T> Closeable subscribe(String key, Class<T> clazz, Consumer<T> consumer);
}
