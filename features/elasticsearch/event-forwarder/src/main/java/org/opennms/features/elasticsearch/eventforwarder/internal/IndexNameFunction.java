package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.apache.camel.component.properties.PropertiesFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The apply method is called on each message to determine in which index it should end up into.
 *
 * http://unicolet.blogspot.com/2015/01/camel-elasticsearch-create-timestamped.html
 *
 * Created:
 * User: unicoletti
 * Date: 11:12 AM 6/24/15
 */
public class IndexNameFunction implements PropertiesFunction {

    private static final Logger LOG = LoggerFactory.getLogger(IndexNameFunction.class);

    public static final String DEFAULT_INDEX_DATE_FORMAT = "yyyy.MM";

    private final SimpleDateFormat m_df;

    public IndexNameFunction() {
        this(DEFAULT_INDEX_DATE_FORMAT);
    }

    public IndexNameFunction(String dateFormat) {
        m_df = new SimpleDateFormat(dateFormat == null ? DEFAULT_INDEX_DATE_FORMAT : dateFormat);
    }

    @Override
    public String getName() {
        return "index";
    }

    @Override
    public String apply(String remainder) {
        return apply(m_df, remainder, new Date());
    }

    public String apply(String remainder, Date date) {
        return apply(m_df, remainder, date);
    }

    public static String apply(SimpleDateFormat df, String remainder, Date date) {
        String result = remainder.toLowerCase() + "-" + df.format(date);

        if(LOG.isTraceEnabled()) {
            LOG.trace("IndexNameFunction.apply=" + result);
        }

        return result;
    }
}
