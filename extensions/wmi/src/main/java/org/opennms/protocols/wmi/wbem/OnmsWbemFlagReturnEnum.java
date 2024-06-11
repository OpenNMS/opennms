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
package org.opennms.protocols.wmi.wbem;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: CE136452
 * Date: Oct 21, 2008
 * Time: 6:45:47 AM
 * To change this template use File | Settings | File Templates.
 *
 * @author ranger
 * @version $Id: $
 */
public enum OnmsWbemFlagReturnEnum {
   
    wbemFlagForwardOnly(32, "wbemFlagForwardOnly"),
    wbemFlagBidirectional(0, "wbemFlagBidirectional"),
    wbemFlagReturnImmediately(16, "wbemFlagReturnImmediately"),
    wbemFlagReturnWhenComplete(0, "wbemFlagReturnWhenComplete"),
    wbemQueryFlagPrototype(2, "wbemQueryFlagPrototype"),
    wbemFlagUseAmendedQualifiers(131072, "wbemFlagUseAmendedQualifiers");

    /** Constant <code>lookup</code> */
    private static final Map<Integer, OnmsWbemFlagReturnEnum> lookup = new HashMap<Integer, OnmsWbemFlagReturnEnum>();
    private int returnFlagValue;
    private String returnFlagName;

    static {
        for (final OnmsWbemFlagReturnEnum s : EnumSet.allOf(OnmsWbemFlagReturnEnum.class))
            lookup.put(s.getReturnFlagValue(), s);
    }

    OnmsWbemFlagReturnEnum(final int returnFlagValue, final String returnFlagName) {
        this.returnFlagValue = returnFlagValue;
        this.returnFlagName = returnFlagName;
    }

    /**
     * <p>Getter for the field <code>returnFlagValue</code>.</p>
     *
     * @return a int.
     */
    public int getReturnFlagValue() { return returnFlagValue; }
    /**
     * <p>Getter for the field <code>returnFlagName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReturnFlagName() { return returnFlagName; }

    /**
     * <p>get</p>
     *
     * @param returnFlagValue a int.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemFlagReturnEnum} object.
     */
    public static OnmsWbemFlagReturnEnum get(final int returnFlagValue) {
        return lookup.get(returnFlagValue);
    }
}

