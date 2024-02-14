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
package org.opennms.features.distributed.kvstore.blob.shell;

import java.util.Objects;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.distributed.kvstore.api.BlobStore;

@Command(scope = "opennms", name = "kv-put-blob", description = "Insert a string record into the blob store")
@Service
public class BlobStorePut implements Action {
    @Reference
    private BlobStore blobStore;

    @Argument(index = 0, description = "The key", required = true)
    private String key;

    @Argument(index = 1, description = "The key's context", required = true)
    private String context;

    @Argument(index = 2, description = "The value to put", required = true)
    private String value;

    @Override
    public Object execute() {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);
        Objects.requireNonNull(value);

        blobStore.put(key, value.getBytes(), context);

        return null;
    }
}
