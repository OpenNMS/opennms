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
package org.opennms.protocols.wmi.wbem.jinterop;

import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.common.JIException;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethod;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;

/**
 * <p>OnmsWbemMethodSetImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemMethodSetImpl implements OnmsWbemMethodSet {
    private IJIDispatch wbemMethodSetDispatch;

    /**
     * <p>Constructor for OnmsWbemMethodSetImpl.</p>
     *
     * @param wbemMethodSetDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemMethodSetImpl(final IJIDispatch wbemMethodSetDispatch) {
        this.wbemMethodSetDispatch = wbemMethodSetDispatch;
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Integer getCount() throws WmiException {
        try {
            return wbemMethodSetDispatch.get("Count").getObjectAsInt();
        } catch(final JIException e) {
            throw new WmiException("Failed to retrieve Method Set count: " + e.getMessage(), e);
        }
    }

        /** {@inheritDoc} */
    @Override
        public OnmsWbemMethod get(final Integer idx) throws WmiException {
        try {
            final IJIComObject enumComObject = wbemMethodSetDispatch.get("_NewEnum").getObjectAsComObject();
            final IJIEnumVariant enumVariant = (IJIEnumVariant) JIObjectFactory.narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));
            OnmsWbemMethod wbemMethod;
            IJIDispatch wbemMethod_dispatch = null;
            for (int i = 0; i < (idx+1); i++) {
                final Object[] values = enumVariant.next(1);
                final JIArray array = (JIArray)values[0];
                final Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++)
                {
                    wbemMethod_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());
                }
            }

            wbemMethod = new OnmsWbemMethodImpl(wbemMethod_dispatch);
            return wbemMethod;
        } catch(final JIException e) {
            throw new WmiException("Failed to enumerate WbemObject variant: " + e.getMessage(), e);
        }
    }

}
