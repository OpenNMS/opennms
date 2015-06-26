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

    Logger logger = LoggerFactory.getLogger(IndexNameFunction.class);

    private SimpleDateFormat df=null;

    public IndexNameFunction() {
        df=new SimpleDateFormat("yyyy.MM");
    }

    public IndexNameFunction(String dateFormat) {
        df=new SimpleDateFormat(dateFormat == null ? "yyyy.MM" : dateFormat);
    }

    @Override
    public String getName() {
        return "index";
    }

    @Override
    public String apply(String remainder) {
        String result=null;
        result=remainder.toLowerCase()+"-"+df.format(new Date());

        if(logger.isTraceEnabled()) {
            logger.trace("IndexNameFunction.apply=" + result);
        }
        return result;
    }

    public String apply(String remainder, Date date) {
            String result=null;
            result=remainder.toLowerCase()+"-"+df.format(date);

            if(logger.isTraceEnabled()) {
                logger.trace("IndexNameFunction.apply=" + result);
            }

            return result;
        }
}
