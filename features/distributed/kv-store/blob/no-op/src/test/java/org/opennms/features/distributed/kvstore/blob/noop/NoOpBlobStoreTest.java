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
package org.opennms.features.distributed.kvstore.blob.noop;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.opennms.features.distributed.kvstore.api.BlobStore;

public class NoOpBlobStoreTest {
    @Test
    public void doesNotPersist() {
        BlobStore kvStore = NoOpBlobStore.getInstance();
        String keyContext = "test";
        kvStore.put(keyContext, new byte[0], keyContext);
        Optional<byte[]> result = kvStore.get(keyContext, keyContext);
        
        assertThat(result.isPresent(), is(false));
    }
}
