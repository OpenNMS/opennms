package org.opennms.plugins.elasticsearch.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The apply method is called on each message to determine in which index it should end up into.
 */
public class IndexNameFunction {

	private static final Logger LOG = LoggerFactory.getLogger(IndexNameFunction.class);

    private SimpleDateFormat df=null;

    public IndexNameFunction() {
        df=new SimpleDateFormat("yyyy.MM");
    }

    public IndexNameFunction(String dateFormat) {
        df=new SimpleDateFormat(dateFormat == null ? "yyyy.MM" : dateFormat);
    }


    public String apply(String rootIndexName) {
        String result=null;
        result=rootIndexName.toLowerCase()+"-"+df.format(new Date());

        if(LOG.isDebugEnabled()) {
            LOG.debug("IndexNameFunction.apply=" + result);
        }
        return result;
    }

    public String apply(String rootIndexName, Date date) {
            String result=null;
            result=rootIndexName.toLowerCase()+"-"+df.format(date);

            if(LOG.isDebugEnabled()) {
                LOG.debug("IndexNameFunction.apply=" + result);
            }

            return result;
        }
}
