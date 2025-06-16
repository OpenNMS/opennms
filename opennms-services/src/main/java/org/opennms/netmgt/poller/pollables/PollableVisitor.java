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
package org.opennms.netmgt.poller.pollables;

/**
 * Represents a PollableVisitor
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollableVisitor {

    /**
     * <p>visitService</p>
     *
     * @param service a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    void visitService(PollableService service);

    /**
     * <p>visitInterface</p>
     *
     * @param interface1 a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    void visitInterface(PollableInterface interface1);

    /**
     * <p>visitNode</p>
     *
     * @param node a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    void visitNode(PollableNode node);

    /**
     * <p>visitNetwork</p>
     *
     * @param network a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    void visitNetwork(PollableNetwork network);

    /**
     * <p>visitContainer</p>
     *
     * @param container a {@link org.opennms.netmgt.poller.pollables.PollableContainer} object.
     */
    void visitContainer(PollableContainer container);

    /**
     * <p>visitElement</p>
     *
     * @param element a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    void visitElement(PollableElement element);

}
