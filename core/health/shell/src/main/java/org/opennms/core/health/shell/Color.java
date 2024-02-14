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
package org.opennms.core.health.shell;

/**
 * ANSI Color Code
 */
public enum Color {
    NoColor("0"),
    Red("0;31"),
    Green("0;32"),
    Yellow("1;33"),
    Blue("0;34");

    private final String ansiCode;

    private Color(final String ansiCode) {
        this.ansiCode = ansiCode;
    }

    public String toAnsi() {
        return ansiCode;
    }
}
