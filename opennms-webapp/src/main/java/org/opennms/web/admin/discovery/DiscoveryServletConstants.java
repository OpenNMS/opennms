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
package org.opennms.web.admin.discovery;

/**
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DiscoveryServletConstants {

    /**
     * Indicates that we're editing the persistent discovery config
     */
    public static final String EDIT_MODE_CONFIG = "config";

    /**
     * Indicates that we're editing a one-time scan config
     */
    public static final String EDIT_MODE_SCAN = "scan";

    /** Constant <code>addSpecificAction="AddSpecific"</code> */
    public static final String addSpecificAction = "AddSpecific";
    /** Constant <code>removeSpecificAction="RemoveSpecific"</code> */
    public static final String removeSpecificAction = "RemoveSpecific";

    /** Constant <code>addIncludeRangeAction="AddIncludeRange"</code> */
    public static final String addIncludeRangeAction = "AddIncludeRange";
    /** Constant <code>removeIncludeRangeAction="RemoveIncludeRange"</code> */
    public static final String removeIncludeRangeAction = "RemoveIncludeRange";

    /** Constant <code>addIncludeUrlAction="AddIncludeUrl"</code> */
    public static final String addIncludeUrlAction = "AddIncludeUrl";
    /** Constant <code>removeIncludeUrlAction="RemoveIncludeUrl"</code> */
    public static final String removeIncludeUrlAction = "RemoveIncludeUrl";

    /** Constant <code>addExcludeUrlAction="AddExcludeUrl"</code> */
    public static final String addExcludeUrlAction = "AddExcludeUrl";
    /** Constant <code>removeExcludeUrlAction="RemoveExcludeUrl"</code> */
    public static final String removeExcludeUrlAction = "RemoveExcludeUrl";

    /** Constant <code>addExcludeRangeAction="AddExcludeRange"</code> */
    public static final String addExcludeRangeAction = "AddExcludeRange";
    /** Constant <code>removeExcludeRangeAction="RemoveExcludeRange"</code> */
    public static final String removeExcludeRangeAction = "RemoveExcludeRange";

    /** Constant <code>saveAndRestartAction="SaveAndRestart"</code> */
    public static final String saveAndRestartAction = "SaveAndRestart";
}
