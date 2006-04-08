/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.snmpif;

import javax.sql.DataSource;


public class FindAll extends SnmpInterfaceMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM snmpinterface");
        compile();
    }
}