package org.opennms.web.outage;

import java.util.ArrayList;
import org.opennms.web.outage.filter.*;


/**
 * Convenience data structure for holding the arguments to an outage query. 
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class OutageQueryParms extends Object 
{
    public OutageFactory.SortStyle sortStyle;
    public OutageFactory.OutageType outageType;
    public ArrayList filters;
    public int limit;
    public int multiple;


    /**
     * Convert the internal (and useful) ArrayList filters object
     * into an array of Filter instances.
     */ 
    public Filter[] getFilters() {
        Filter[] filters = new Filter[this.filters.size()];                        
        filters = (Filter[])this.filters.toArray(filters);

        return filters;
    }
}
