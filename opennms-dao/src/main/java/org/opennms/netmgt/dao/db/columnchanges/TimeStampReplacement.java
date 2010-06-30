
/**
 * <p>TimeStampReplacement class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;
public class TimeStampReplacement implements ColumnChangeReplacement {
    private final Date m_replacement;
    
    /**
     * <p>Constructor for TimeStampReplacement.</p>
     *
     * @param value a {@link java.util.Date} object.
     */
    public TimeStampReplacement(Date value) {
        m_replacement = value;
    }

    /** {@inheritDoc} */
    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
        return m_replacement;
    }
    
    /**
     * <p>addColumnIfColumnIsNew</p>
     *
     * @return a boolean.
     */
    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    /**
     * <p>close</p>
     */
    public void close() {
    }
}
