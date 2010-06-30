//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 13: Genericize List passed to send method. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.MatchTable;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>HttpNotificationStrategy class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class HttpNotificationStrategy implements NotificationStrategy {

    private List<Argument> m_arguments;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    /** {@inheritDoc} */
    public int send(List<Argument> arguments) {
        
        m_arguments = arguments;
        
        String url = getUrl();
        if (url == null) {
            log().warn("send: url argument is null, HttpNotification requires a URL");
            return 1;
        }
        
        HttpClient client = new HttpClient();
        HttpMethod method = null;
        NameValuePair[] posts = getPosts();
                
        if (posts == null) {
            method = new GetMethod(url);
            log().info("send: No \"post-\" arguments..., continuing with an HTTP GET using URL: "+url);
        } else {
            log().info("send: Found \"post-\" arguments..., continuing with an HTTP POST using URL: "+url);
            for (Iterator<Argument> it = m_arguments.iterator(); it.hasNext();) {
                Argument arg = it.next();
                log().debug("send: post argument: "+arg.getSwitch() +" = "+arg.getValue());
            }
            method = new PostMethod(url);
            ((PostMethod)method).addParameters(posts);
        }

        String contents = null;
        int statusCode = -1;
        try {
            statusCode = client.executeMethod( method );
            contents = method.getResponseBodyAsString();
            log().info("send: Contents is: "+contents);
        } catch (HttpException e) {
            log().error("send: problem with HTTP post: "+e);
            throw new RuntimeException("Problem with HTTP post: "+e.getMessage());
        } catch (IOException e) {
            log().error("send: IO problem with HTTP post/response: "+e);
            throw new RuntimeException("Problem with HTTP post: "+e.getMessage());
        } finally {
            method.releaseConnection();
        }
        
        doSql(contents);
        
        return statusCode;
    }

    private void doSql(String contents) {
        if (getSql() == null) {
            log().info("send: optional sql argument is null.");
            return;
        }

        if (contents == null) {
            log().info("doSql: HTTP reply is null");
            return;
        }

        log().debug("send: compiling expression: "+getSwitchValue("result-match"));
        Pattern p = Pattern.compile(getSwitchValue("result-match"));
        Matcher m = p.matcher(contents);
        if (m.matches()) {
            log().debug("send: compiled expression ready to run sql: "+getSql());
            MatchTable matches = new MatchTable(m);
            String sqlString = PropertiesUtils.substitute(getSql(), matches);
            log().debug("send: running sql: "+sqlString);
            JdbcTemplate template = new JdbcTemplate(DataSourceFactory.getInstance());
            template.execute(sqlString);
        } else {
            log().info("send: result didn't match, not running sql");
        }
    }

    private NameValuePair[] getPosts() {
        List<Argument> args = getArgsByPrefix("post-");
        NameValuePair[] posts = new NameValuePair[args.size()];
        int cnt = 0;
        for (Iterator<Argument> it = args.iterator(); it.hasNext();) {
            Argument arg = it.next();
            String argSwitch = arg.getSwitch().substring("post-".length());
            if (arg.getValue() == null) {
                arg.setValue("");
            }
            posts[cnt++] = new NameValuePair(argSwitch, arg.getValue().equals("-tm") ? getMessage() : arg.getValue());
        }
        return posts;
    }

    private String getMessage() {
        String message = "no notification text message defined for the \"-tm\" switch.";
        for (Iterator<Argument> it = m_arguments.iterator(); it.hasNext();) {
            Argument arg = it.next();
            if (arg.getSwitch().equals("-tm"))
                message = arg.getValue();
        }
        log().debug("getMessage: "+message);
        return message;
    }

    private List<Argument> getArgsByPrefix(String argPrefix) {
        List<Argument> args = new ArrayList<Argument>();
        for (Iterator<Argument> it = m_arguments.iterator(); it.hasNext();) {
            Argument arg = it.next();
            if (arg.getSwitch().startsWith(argPrefix)) {
                args.add(arg) ;
            }
        }
        return args;
    }
    
    private String getSql() {
        return getSwitchValue("sql");
    }

    private String getUrl() {
        return getSwitchValue("url");
    }

    /**
     * Helper method to look into the Argument list and return the associaated value.
     * If the value is an empty String, this method returns null.
     * @param argSwitch
     * @return
     */
    private String getSwitchValue(String argSwitch) {
        String value = null;
        for (Iterator<Argument> it = m_arguments.iterator(); it.hasNext();) {
            Argument arg = it.next();
            if (arg.getSwitch().equals(argSwitch)) {
                value = arg.getValue();
            }
        }
        if (value != null && value.equals(""))
            value = null;
        
        return value;
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

}
