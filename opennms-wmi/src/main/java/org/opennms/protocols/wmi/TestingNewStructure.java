package org.opennms.protocols.wmi;

import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethod;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.wbem.OnmsWbemCimTypeEnum;

import java.util.List;

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
public class TestingNewStructure {
    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        try {
            // Connect to the WMI server.
            WmiClient wmiClient = new WmiClient("localhost");
            wmiClient.connect("CHILDRENSNT", "CE136452", "aj7162007");

            OnmsWbemObjectSet wos = wmiClient.performExecQuery("Select * From Win32_NTLogEvent");
            System.out.println("The number of objects retrieved: " + wos.count());
            for(int i=0; i<wos.count(); i++) {
                OnmsWbemObject obj = wos.get(i);
                if(obj == null)
                    System.out.println("Received null object.");
                //System.out.println("+++++ Testing Object PropertySet +++++");
                OnmsWbemPropertySet propSet = obj.getWmiProperties();
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
        } catch (WmiException e) {
            System.out.println("Caught exception: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
