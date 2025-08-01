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
package org.opennms.enlinkd.generator;

public class TopologyContext {

    private final TopologyGenerator.ProgressCallback progressCallback;

    private final TopologyPersister topologyPersister;

    TopologyContext(final TopologyGenerator.ProgressCallback progressCallback, TopologyPersister topologyPersister){
        this.progressCallback = progressCallback;
        this.topologyPersister = topologyPersister;
    }

    public void currentProgress(String msg){
        this.progressCallback.currentProgress(msg);
    }

    public void currentProgress(String msg, Object...args){
        this.progressCallback.currentProgress(msg, args);
    }

    public TopologyPersister getTopologyPersister(){
        return this.topologyPersister;
    }
}
