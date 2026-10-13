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
package org.apache.activemq.nms20397;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lives under org.apache.activemq on purpose: ActiveMQ's default trusted-package
 * list would deserialize it, so only an empty trustedPackages list refuses it.
 */
public class DeserializationProbe implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final AtomicBoolean DESERIALIZED = new AtomicBoolean();

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        DESERIALIZED.set(true);
        in.defaultReadObject();
    }
}
