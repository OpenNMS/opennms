/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.alarm;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByAlarmId extends AlarmMappingQuery {

    public FindByAlarmId(DataSource ds) {
        super(ds, "FROM alarms as a WHERE alarmid = ?");
        super.declareParameter(new SqlParameter("alarmid", Types.INTEGER));
        compile();
    }
    
}