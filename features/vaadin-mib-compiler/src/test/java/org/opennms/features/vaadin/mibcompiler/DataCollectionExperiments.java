package org.opennms.features.vaadin.mibcompiler;

import java.io.File;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;

import org.junit.Test;

public class DataCollectionExperiments {

    @Test
    public void testDataCollection() throws Exception {
        MibLoader loader = new MibLoader();
        loader.addDir(new File("src/test/resources"));
        Mib mib = loader.load(new File("src/test/resources/IF-MIB.txt"));
        //printNode(mib.getRootSymbol());
        printNode(mib);
    }

    // Sequential
    // Has more information, for example ifTable
    public void printNode(Mib mib) {
        for (Object o : mib.getAllSymbols()) {
            if (o instanceof MibValueSymbol) {
                MibValueSymbol node = (MibValueSymbol) o;
                if (node.getType() instanceof SnmpObjectType && !node.isTable() && !node.isTableRow()) {
                    String valueType = ((SnmpObjectType) node.getType()).getSyntax().toString();
                    System.err.print(node.getName() + " = " + node.getValue() + " (" + valueType + ")");
                    if (node.isTableColumn()) { // GenericIndexResource
                        System.err.println(", from " + node.getParent().getParent().getName());
                    } else { // NodeResource
                        System.err.println(", it is scalar!");
                    }
                }
            }
        }
    }

    // Hierarchical
    public void printNode(MibValueSymbol node) {
        if (node.getType() instanceof SnmpObjectType && !node.isTable() && !node.isTableRow()) {
            System.err.print(node.getName() + " = " + node.getValue());
            if (node.isTableColumn()) { // GenericIndexResource
                System.err.println(", from " + node.getParent().getParent().getName());
            } else { // NodeResource
                System.err.println(", it is scalar!");
            }
        }
        if (node.getChildCount() > 0) {
            for (MibValueSymbol child : node.getChildren()) {
                printNode(child);
            }
        }
    }

}
