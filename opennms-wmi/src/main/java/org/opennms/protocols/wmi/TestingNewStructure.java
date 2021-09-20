/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.wmi;

import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;

/**
 * Created by IntelliJ IDEA.
 * User: CE136452
 * Date: Oct 17, 2008
 * Time: 1:53:21 PM
 * To change this template use File | Settings | File Templates.
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class TestingNewStructure {
    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(final String[] args) {
        try {
            // Connect to the WMI server.
            final WmiClient wmiClient = new WmiClient("localhost");
            wmiClient.connect("CHILDRENSNT", "CE136452", "aj7162007", WmiParams.WMI_DEFAULT_NAMESPACE);

            final OnmsWbemObjectSet wos = wmiClient.performExecQuery("Select * From Win32_NTLogEvent");
            System.out.println("The number of objects retrieved: " + wos.count());
            for(int i=0; i<wos.count(); i++) {
                final OnmsWbemObject obj = wos.get(i);
                if(obj == null)
                    System.out.println("Received null object.");
                //System.out.println("+++++ Testing Object PropertySet +++++");
                final OnmsWbemPropertySet propSet = obj.getWmiProperties();
                //System.out.println("Object has " + propSet.count() + " properties available.");
                System.out.println("The service ("+propSet.getByName("EventIdentifier").getWmiValue()+") is: " + propSet.getByName("Message").getWmiValue());
                //for(int b=0; b<propSet.count(); b++) {
                //    OnmsWbemProperty prop = propSet.get(b);
                //    System.out.println("Property named: " + prop.getWmiName());
                //    System.out.println("Property origin: " + prop.getWmiOrigin());
                //    System.out.println("Property is array: " + prop.getWmiIsArray());
                //    System.out.println("Property is local: " + prop.getWmiIsLocal());
                //    System.out.println("Property value: " + prop.getWmiValue());
                //    System.out.println("Property CIMType: " + OnmsWbemCimTypeEnum.get(prop.getWmiCIMType()).getCimName() );
                //}
            }
            // lets do some logic here....
//            OnmsWbemObjectSet wos = wmiClient.performInstanceOf("Win32_Keyboard");
//
//            System.out.println("The number of objects retrieved: " + wos.count());
//            for(int i=0; i<wos.count(); i++) {
//                OnmsWbemObject obj = wos.get(0);
//                if(obj == null)
//                    System.out.println("Received null object.");
//
//                System.out.println("+++++ Testing OnmsWbemMethodSet and Mehod +++++");
//                OnmsWbemMethodSet methodSet = obj.getWmiMethods();
//                System.out.println("object has " + methodSet.getCount() + " methods available.");
//                for(int a=0;a<methodSet.getCount(); a++) {
//                    OnmsWbemMethod method = methodSet.get(a);
//                    System.out.println("Object method Name: " + method.getWmiName());
//                    System.out.println("Object method Origin: " + method.getWmiOrigin());
//                }
//
//                System.out.println("+++++ Testing OnmsWbemObjectPath +++++");
//                OnmsWbemObjectPath objPath = obj.getWmiPath();
//                System.out.println("Object Path is: " + objPath.getWmiPath());
//                System.out.println("Object ParentNamespace is: " + objPath.getWmiParentNamespace());
//                System.out.println("Object Namespace is: " + objPath.getWmiNamespace());
//                System.out.println("Object Locale is: " + objPath.getWmiLocale());
//                System.out.println("Object DisplayName is: " + objPath.getWmiDisplayName());
//                System.out.println("Object Class is: " + objPath.getWmiClass());
//                System.out.println("Object Authority is: " + objPath.getWmiAuthority());
//                System.out.println("Object RelPath is: " + objPath.getWmiRelPath());
//                System.out.println("Object Server is: " + objPath.getWmiServer());
//                System.out.println("Object IsClass is: " + objPath.getWmiIsClass());
//                System.out.println("Object IsSingleton is: " + objPath.getWmiIsSingleton());
//
//                System.out.println("+++++ Testing Getting Object Text +++++");
//                System.out.println(obj.getWmiObjectText());
//
//                System.out.println("+++++ Testing Object PropertySet +++++");
//                OnmsWbemPropertySet propSet = obj.getWmiProperties();
//                System.out.println("Object has " + propSet.count() + " properties available.");
//                for(int b=0; b<propSet.count(); b++) {
//                    OnmsWbemProperty prop = propSet.get(b);
//                    System.out.println("Property named: " + prop.getWmiName());
//                    System.out.println("Property origin: " + prop.getWmiOrigin());
//                    System.out.println("Property is array: " + prop.getWmiIsArray());
//                    System.out.println("Property is local: " + prop.getWmiIsLocal());
//                    System.out.println("Property value: " + prop.getWmiValue());
//                    System.out.println("Property CIMType: " + OnmsWbemCimTypeEnum.get(prop.getWmiCIMType()).getCimName() );
//
//
//                }
//
//            }
            
            // Disconenct from the WMI server.
            wmiClient.disconnect();
        } catch (final WmiException e) {
            System.out.println("Caught exception: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
