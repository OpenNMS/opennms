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
package org.opennms.core.ipc.sink.offheap;

import org.opennms.core.ipc.sink.api.ReadFailedException;

import java.util.Map;

public interface DataBlock<T> {
    String getName();

    boolean enqueue(String key, T message);

    Map.Entry<String, T> peek() throws InterruptedException, ReadFailedException;

    Map.Entry<String, T> dequeue() throws InterruptedException, ReadFailedException;

    /**
     * Make sure queue is ready
     */
    void notifyNextDataBlock() throws ReadFailedException;

    void setNextDataBlock(DataBlock<T> dataBlock);

    int size();
}
