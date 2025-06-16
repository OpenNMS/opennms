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
package org.opennms.features.topology.app.internal.menu;

import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * A simple implementation for a {@link MenuItem} which always returns
 * true for {@link #isVisible(List, OperationContext)} and {@link #isEnabled(List, OperationContext)},
 * but false for {@link #isCheckable()} and {@link #isChecked(List, OperationContext)}
 *
 * @author mvrueden
 */
public class SimpleMenuItem extends AbstractMenuItem {

    /**
     * The command to execute. May be null.
     */
    private MenuCommand command;

    public SimpleMenuItem(String label) {
        setLabel(Objects.requireNonNull(label));
        setCheckable(false);
    }

    @Override
    public boolean isVisible(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean isEnabled(List<VertexRef> targets, OperationContext operationContext) {
        if (getCommand() != null) {
            return true;
        } else {
            return !getChildren().isEmpty();
        }
    }

    @Override
    public boolean isChecked(List<VertexRef> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public MenuCommand getCommand() {
        return command;
    }

    public void setCommand(MenuCommand command) {
        this.command = command;
    }
}
