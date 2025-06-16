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
package org.opennms.core.fiber;

/**
 * <p>
 * This class is used to extend the <code>Fiber</code> interface so that is
 * has a concept of a life cycle. Prior to starting the fiber the
 * <code>init</code> method will be invoked. Likewise, prior to garbage
 * collection the <code>destroy</code> method should be invoked.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public interface InitializableFiber extends Fiber {
    /**
     * This method is used to start the initilization process of the
     * <code>Fiber</code>, which should eventually transition to a
     * <code>RUNNING</code> status.
     */
    public void init();

    /**
     * This method is used to stop a currently running <code>Fiber</code>.
     * Once invoked the <code>Fiber</code> should begin it's shutdown process.
     * Depending on the implementation, this method may block until the
     * <code>Fiber</code> terminates.
     */
    public void destroy();
}
