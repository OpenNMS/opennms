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

/**
 * This interface is used on objects that are inherently
 * asynchronous (like Mina or Netty library calls). The
 * {@link #supplyAsyncThenAccept(Callback)} method will
 * invoke an async operation that uses the {@link Callback}
 * as the completion and exception handler for the operation.
 *
 * @author Seth
 * @author brozow
 */
public interface Async<T> {
    
    /**
     * <p>submit</p>
     *
     * @param cb a {@link org.opennms.core.tasks.Callback} object.
     * @param <T> a T object.
     */
    void supplyAsyncThenAccept(Callback<T> cb);

}
