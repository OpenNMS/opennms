package org.opennms.netmgt.threshd;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.utils.IfLabel;

public class JdbcIfInfoGetter implements IfInfoGetter {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.threshd.IfInfoGetter#getIfInfoForNodeAndLabel(int, java.lang.String)
     */
    public Map<String, String> getIfInfoForNodeAndLabel(int nodeId, String ifLabel) {
        // Get database connection
        java.sql.Connection dbConn = null;
        try {
            dbConn = DataSourceFactory.getInstance().getConnection();
        } catch (SQLException e) {
            //log().error("checkIfDir: Failed getting connection to the database: " + e, e);
            throw new UndeclaredThrowableException(e);
        }
    
        // Make certain we close the connection
        Map<String, String> ifInfo = new HashMap<String, String>();
        try {
            ifInfo = IfLabel.getInterfaceInfoFromIfLabel(dbConn, nodeId, ifLabel);
        } catch (SQLException e) {
            /*
             * Logging a warning message but processing will
             * continue for
             * this thresholding event, when the event is
             * created it
             * will be created with an interface value set
             * to the primary
             * SNMP interface address and an event source
             * set to
             * <datasource>:<ifLabel>.
             */
            //log().warn("Failed to retrieve interface info from database using ifLabel '" + ifLabel + "': " + e, e);
        } finally {
            // Done with the database so close the connection
            try {
                if (dbConn != null) {
                    dbConn.close();
                }
            } catch (SQLException e) {
                //log().info("checkIfDir: SQLException while closing database connection: " + e, e);
            }
        }
        return ifInfo;
    }

}
