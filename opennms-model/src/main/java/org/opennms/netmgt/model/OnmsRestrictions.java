package org.opennms.netmgt.model;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;

public class OnmsRestrictions {
    private static final StringType STRING_TYPE = new StringType();
    
    /**
     * Performs an iplike match on the ipAddr column of the current table.
     * 
     * @param value iplike match
     * @return SQL restriction for this iplike match
     */
    public static Criterion ipLike(String value) {
        return Restrictions.sqlRestriction("iplike({alias}.ipAddr, ?)", value, STRING_TYPE);
    }
}
