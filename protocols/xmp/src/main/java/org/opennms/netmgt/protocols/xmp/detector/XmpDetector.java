/*
* OCA CONTRIBUTION ACKNOWLEDGEMENT - NOT PART OF LEGAL BOILERPLATE
* DO NOT DUPLICATE THIS COMMENT BLOCK WHEN CREATING NEW FILES!
*
* This file was contributed to the OpenNMS(R) project under the
* terms of the OpenNMS Contributor Agreement (OCA).  For details on
* the OCA, see http://www.opennms.org/index.php/Contributor_Agreement
*
* Contributed under the terms of the OCA by:
*
* Bobby Krupczak <rdk@krupczak.org>
* THE KRUPCZAK ORGANIZATION, LLC
* http://www.krupczak.org/
*/

/**
 * OpenNMS XMP Detector allows for discovery of service/protocols via 
 * provisiond.  Our detector is pretty simple right now.  All it does is
 * attempt to establish an XmpSession with a system and if it succeeds,
 * it queries a few core MIB variables and returns success.
 * Future enhancements could include more capability information like
 * supported MIBs, modules/plugins, and various dependencies to
 * determine if something is a server for some service (noted by an
 * inbound dependency relationship).
 */

package org.opennms.protocols.xmp.detector;

import java.net.InetAddress;
import java.util.Date;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.AbstractDetector;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.krupczak.xmp.*;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;    
import org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory;
import org.opennms.netmgt.protocols.xmp.collector.XmpCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * XmpDetector class
 * @author rdk <rdk@krupczak.org>
 * @version $Id: $
 *
 */
@Scope("prototype")
public class XmpDetector implements SyncServiceDetector
{
    // class variables

    private static final String DEFAULT_SERVICE_NAME = "XMP";
    private static final Logger LOG = LoggerFactory.getLogger(XmpDetector.class);
    private static int XMP_DEFAULT_TIMEOUT = 3000;
    private static int XMP_DEFAULT_RETRIES = 1;
    
    // instance variables
    SocketOpts sockopts;
    XmpConfig protoConfig;
    int xmpPort;
    int xmpTimeout;
    int xmpRetries;
    String xmpAuthenUser;
    String xmpServiceName; 
    String m_ipMatch;
    Date createTimeDate;

    /* 
     * @param serviceName a {@link java.lang.String} object
     * @param port an int specifying tcp port number
     */
    
    public XmpDetector(String serviceName, int port)
    {
	//super(serviceName,port,XMP_DEFAULT_TIMEOUT,XMP_DEFAULT_RETRIES);
	
        // set default config
        xmpPort = port;
        xmpAuthenUser = new String("xmpUser"); 
        xmpTimeout = XMP_DEFAULT_TIMEOUT; /* millseconds */
        xmpRetries = XMP_DEFAULT_RETRIES;

        // get socket opts
        sockopts = new SocketOpts();

        xmpServiceName = new String(serviceName);

	// very important to set to null, not 0-len string
	// as provisiond's ip range matching functionality
	// will not correctly match if 0-len instead of null
	
	//m_ipMatch = new String("");
	m_ipMatch = null;  

	createTimeDate = new Date();

	if (LOG == null) {
	   System.out.println("XmpDetector created, but null LOG");
	}
	else {
	    LOG.debug("XmpDetector created, service "+xmpServiceName+" at "+createTimeDate);
	}

    } /* XmpDetector */

    public XmpDetector()
    {
	this(DEFAULT_SERVICE_NAME,Xmp.XMP_PORT);
        
    } /* XmpDetector */

    public String getServiceName() {
	LOG.debug("XmpDetector: getServiceName");
	return xmpServiceName;
    }
  
    public void setServiceName(String newServiceName) 
    {
	LOG.debug("XmpDetector: setServiceName to "+newServiceName);
    	xmpServiceName = new String(newServiceName);         
    }

    public void init() { onInit(); }

    public void onInit()
    {
        LOG.debug("XmpDetector: onInit starting");
	
        // try to get configuration
        try { 
            XmpConfig protoConfig;

	    XmpConfigFactory.init();
            protoConfig = XmpConfigFactory.getInstance().getXmpConfig();
            if (protoConfig.hasPort())
               xmpPort = protoConfig.getPort();
            if (protoConfig.hasTimeout())
               xmpTimeout = protoConfig.getTimeout();
            if (protoConfig.getAuthenUser() != null)
               xmpAuthenUser = protoConfig.getAuthenUser();

            sockopts.setConnectTimeout(xmpTimeout);	    

        } catch (Throwable e) {
            if (LOG != null)
               LOG.error("XmpDetector: no config factory, using defaults");   
            else
	       System.out.println("XmpDetector: null LOG");
        }
    }

    public int getPort()
    {
        LOG.debug("XmpDetector: getPort");
	return xmpPort;
    }

    public void setPort(int newPort)
    {
        LOG.debug("XmpDetector: setPort to "+newPort);
	xmpPort = newPort;
    }

    public void setIpMatch(String ipMatch)
    {
	LOG.debug("XmpDetector: setIpMatch to "+ipMatch);
	
	m_ipMatch = ipMatch;
    }

    public String getIpMatch()
    {
        LOG.debug("XmpDetector: getIpMatch returning '"+m_ipMatch+"'");
	return m_ipMatch;
    }

    public int getTimeout()
    {
        LOG.debug("XmpDetector: getTimeout returning "+xmpTimeout);
	return xmpTimeout;
    }

    public void setTimeout(int newTimeout) 
    {
        LOG.debug("XmpDetector: setTimeout to "+newTimeout);
        xmpTimeout = newTimeout; 
        sockopts.setConnectTimeout(xmpTimeout);
    }

    public void dispose()
    {
	LOG.debug("XmpDetector: dispose invoked");
	
	// dispose of anything like sessions, etc.
        // no need to dispose SocketOpts
    }

    @Override
    public final boolean isServiceDetected(InetAddress address)
    {
        XmpSession aSession;
        XmpMessage aReply;
        XmpVar[] vars,replyVars;

	LOG.debug("XmpDetector: isServiceDetected starting to query "+address);
	
        // try to establish session
        aSession = new XmpSession(sockopts,address,xmpPort,xmpAuthenUser);
        if (aSession == null) {
	   LOG.debug("XmpDetector: null session to "+address);
	   return false;
        }

        LOG.debug("XmpDetector: isServiceDetected session established with "+address);
        // query for core.sysName, core.sysDescr, 
        // core.sysUpTime, core.xmpdVersion
        vars = new XmpVar[] {
	    new XmpVar("core","sysName","","",Xmp.SYNTAX_NULLSYNTAX),
	    new XmpVar("core","sysDescr","","",Xmp.SYNTAX_NULLSYNTAX),
	    new XmpVar("core","sysUpTime","","",Xmp.SYNTAX_NULLSYNTAX),
	    new XmpVar("core","xmpdVersion","","",Xmp.SYNTAX_NULLSYNTAX),
        };
      
        if ((aReply = aSession.queryVars(vars)) == null) {
            LOG.debug("XmpDetector: isServiceDetected no vars from "+address);
	    aSession.closeSession();
            return false;
        }

        aSession.closeSession();

	// log what we retrieved
        if ((replyVars = aReply.getMIBVars()) == null) {
            LOG.debug("XmpDetector: isServiceDetected no replyVars for "+address);
            return false;

        } /* if replyVars == null */

        LOG.debug("XmpDetector: isServiceDetected "+address+" reports "+
                   replyVars[0].getValue()+","+
                   replyVars[1].getValue());
        LOG.debug("XmpDetector: isServiceDetected true for "+address);

        LOG.debug("XmpDetector: isServiceDetected true for "+address);
	
	return true;

    } /* isServiceDetected */

} /* class XmpDetector */
