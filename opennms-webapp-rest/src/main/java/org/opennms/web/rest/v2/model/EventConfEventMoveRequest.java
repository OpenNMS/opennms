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
package org.opennms.web.rest.v2.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Locale;

/**
 * A single move of one event within its source's evaluation order (1 = evaluated first).
 */
@Schema(name = "EventConfEventMoveRequest",
        description = "Moves one event within its source's evaluation order (1 = evaluated first).")
public class EventConfEventMoveRequest {

    public enum Mode {TOP, BOTTOM, UP, DOWN, POSITION}

    @Schema(description = "How to move the event: `top`, `bottom`, `up`, `down` or `position`.",
            example = "position", allowableValues = {"top", "bottom", "up", "down", "position"})
    private String mode;

    @Schema(description = "Target position for mode `position`, 1-based (1 = evaluated first); ignored otherwise.",
            example = "1")
    private Integer position;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * @throws IllegalArgumentException when the mode is absent or not one of the allowed values
     */
    public Mode resolveMode() {
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("mode is required: top, bottom, up, down or position");
        }
        try {
            return Mode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown mode '" + mode + "': expected top, bottom, up, down or position");
        }
    }
}
