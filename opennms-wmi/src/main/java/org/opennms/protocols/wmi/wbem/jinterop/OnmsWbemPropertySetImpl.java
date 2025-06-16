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
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;

/**
 * <p>OnmsWbemPropertySetImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemPropertySetImpl implements OnmsWbemPropertySet {
    private IJIDispatch wbemPropertySetDispatch;

    /**
     * <p>Constructor for OnmsWbemPropertySetImpl.</p>
     *
     * @param wbemPropertySetDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemPropertySetImpl(final IJIDispatch wbemPropertySetDispatch) {
        this.wbemPropertySetDispatch = wbemPropertySetDispatch;
    }

    /**
     * <p>count</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Integer count() throws WmiException {
        try {
            final JIVariant Count = wbemPropertySetDispatch.get("Count");
            return Count.getObjectAsInt();
        } catch (final JIException e) {
            throw new WmiException("Retrieving Count failed: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsWbemProperty get(final Integer idx) throws WmiException {
        try {
            final IJIComObject enumComObject = wbemPropertySetDispatch.get("_NewEnum").getObjectAsComObject();
            final IJIEnumVariant enumVariant = (IJIEnumVariant) JIObjectFactory.narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));
            OnmsWbemProperty wbemObj;
            IJIDispatch wbemProperty_dispatch = null;
            for (int i = 0; i < (idx+1); i++) {
                final Object[] values = enumVariant.next(1);
                final JIArray array = (JIArray)values[0];
                final Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++) {
                    wbemProperty_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());
                }
            }

            wbemObj = new OnmsWbemPropertyImpl(wbemProperty_dispatch);
            return wbemObj;
        } catch(final JIException e) {
            throw new WmiException("Failed to enumerate WbemProperty variant: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsWbemProperty getByName(final String name) throws WmiException {
        try {
            final IJIComObject enumComObject = wbemPropertySetDispatch.get("_NewEnum").getObjectAsComObject();
            final IJIEnumVariant enumVariant = (IJIEnumVariant) JIObjectFactory.narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));

            IJIDispatch wbemProperty_dispatch = null;
            for (int i = 0; i < count(); i++) {
                final Object[] values = enumVariant.next(1);
                final JIArray array = (JIArray)values[0];
                final Object[] arrayObj = (Object[])array.getArrayInstance();
                for(int j = 0; j < arrayObj.length; j++) {
                    wbemProperty_dispatch = (IJIDispatch)JIObjectFactory.narrowObject(((JIVariant)arrayObj[j]).getObjectAsComObject());

                    // Check the name
                    final JIVariant variant = wbemProperty_dispatch.get("Name");
                    if(variant.getObjectAsString2().equalsIgnoreCase(name)) {
                        return new OnmsWbemPropertyImpl(wbemProperty_dispatch);
                    }
                }
            }

            throw new WmiException("Property Name '" + name + "' not found.");
        } catch(final JIException e) {
            throw new WmiException("Failed to enumerate WbemProperty variant: " + e.getMessage(), e);
        }
    }
}
