
package org.opennms.web.notification;

import java.util.ArrayList;


/**
 * Convenience data structure for holding the arguments to an notice query. 
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NoticeQueryParms extends Object 
{
    public NoticeFactory.SortStyle sortStyle;
    public NoticeFactory.AcknowledgeType ackType;
    public ArrayList filters;
    public int limit;
    public int multiple;


    /**
     * Convert the internal (and useful) ArrayList filters object
     * into an array of EventFactory.Filter instances.
     */ 
    public NoticeFactory.Filter[] getFilters() {
        NoticeFactory.Filter[] filters = new NoticeFactory.Filter[this.filters.size()];                        
        filters = (NoticeFactory.Filter[])this.filters.toArray( filters );
        return( filters );
    }
}
