//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 20: System.out.println/printStackTrace -> log().error. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.vulnscand;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;

/**
 * <p>NessusParser class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NessusParser {
    private static NessusParser instance;

    // Lots o' regular expressions
    private static RE cveId = null;

    private static RE cveString = null;

    private static RE riskFactor = null;

    private static RE semicolonSingle = null;

    private static RE semicolonMulti = null;

    private static RE multipleAsterisks = null;

    private static RE nessusLine = null;

    private static RE nessusSentence = null;

    private static RE nessusTag = null;

    private static RE nessusInside = null;

    // private static RE noPeriod = null;
    private static RE tooManyBreaks = null;

    private static RE leadingOrTrailingBreaks = null;

    private static RE protocolWithPort = null;

    private static RE protocolWithoutPort = null;

    private static RE indeterminate = null;

    // private static RE cleared = null; // Not applicable
    private static RE informational = null;

    private static RE normal = null;
    private static RE normal2 = null;

    private static RE warning = null;

    private static RE minor = null;

    private static RE critical = null;

    private static RE major = null;

    private static RE greaterThan = null;

    private static RE lessThan = null;

    private static RE greaterThanToken = null;

    private static RE lessThanToken = null;

    private static final String GREATER_THAN_TOKEN = "GREATERTHANSIGN";

    private static final String GREATER_THAN_ENTITY = "&gt;";

    private static final String LESS_THAN_TOKEN = "LESSTHANSIGN";

    private static final String LESS_THAN_ENTITY = "&lt;";

    private NessusParser() {
        initREs();
    }

    private void initREs() {
        // The object takes the performance hit of compiling all of these
        // expressions at once, but that should be the only time they are
        // compiled
        try {
            // Wipe any asterisks following a carriage return also
            semicolonSingle = new RE("[:space:]*;([:space:]|\\*)*");
            semicolonMulti = new RE("([:space:]*;){2,}([:space:]|\\*)*");

            // There are lots of multiple asterisks in the messages for emphasis
            multipleAsterisks = new RE("([:space:]*\\*){2,}[:space:]*");

            // Replace all Nessus product names for cleaner integration
            nessusLine = new RE("^[Nn]essus[Dd]{0,1}");
            nessusSentence = new RE("\\.([:space:]|;)*[Nn]essus[Dd]{0,1}");
            nessusTag = new RE(">([:space:]|;)*[Nn]essus[Dd]{0,1}");
            nessusInside = new RE("[Nn]essus[Dd]{0,1}");

            // No period preceeding a <br /> tag
            // noPeriod = new RE("([A-z0-9]{1})[:space:]*<br />");

            // CVE ID numbers
            // Can begin with "CVE" or "CAN"
            cveId = new RE("[Cc](([Vv][Ee])|([Aa][Nn]))-[0-9]{4}-[0-9]{4}");
            // CVE ID number string
            cveString = new RE("[:space:]*[Cc][Vv][Ee][:space:]*:[:space:]*([^;]*)");

            // Risk factor string
            riskFactor =  new RE("[:space:]*[Rr]isk[:space:]*[Ff]actor[:space:]*:([:space:]|;)*([^;{1,2}]*)");
		          //Risk factor :;;None
	    informational = new RE("([Ii]nfo)");
            normal =      new RE("([Nn]one)");
            normal2 =      new RE("(;;[Nn]one)");
            warning =     new RE("([Ll]ow)");
            minor =       new RE("([Mm]edium)");
            major = 	  new RE("([Hh]igh)");
            critical =    new RE("([Cc]ritical)|([Ss]erious)");

            // Duplicate, trailing <br /> tags
            tooManyBreaks = new RE("(<br />){3,}");
            leadingOrTrailingBreaks = new RE("((<br />)+$)|(^(<br />)+)");

            // Protocol strings, with and without port number
            protocolWithPort = new RE("[:space:]*[:alnum:]+[:space:]\\(([:alnum:]+)/([:alnum:]+)\\)[:space:]*");
            protocolWithoutPort = new RE("[:space:]*[:alnum:]+/([:alnum:]+)[:space:]*");

            greaterThan = new RE(">");
            lessThan = new RE("<");

            greaterThanToken = new RE(GREATER_THAN_TOKEN);
            lessThanToken = new RE(LESS_THAN_TOKEN);
        } catch (org.apache.regexp.RESyntaxException ex) {
            log().error("Regex syntax error in NessusScan.java. Correct this error and rebuild: " + ex, ex);
        }
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.vulnscand.NessusParser} object.
     */
    public static NessusParser getInstance() {
        if (instance == null) {
            instance = new NessusParser();

        }
        return instance;
    }

    /**
     * <p>main</p>
     *
     * @param argv an array of {@link java.lang.String} objects.
     */
    public static void main(String[] argv) {
        // Little tester function used to parse events
        NessusParser myParser = NessusParser.getInstance();

        System.out.println(myParser.parseDescr(argv[0]));
    }

    /**
     * <p>parsePort</p>
     *
     * @param portString a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.vulnscand.PortValues} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public PortValues parsePort(String portString) throws IllegalArgumentException {
        PortValues retval = new PortValues();

        // If the portString has port and protocol info...
        if (protocolWithPort.match(portString)) {
            try {
                retval.port = Integer.parseInt(protocolWithPort.getParen(1).trim());
            } catch (NumberFormatException ex) {
                log().error("Cannot parse port into an integer: " + portString, ex);
            }
            retval.protocol = protocolWithPort.getParen(2).trim();
        }
        // Otherwise, if it is just a general info...
        else if (protocolWithoutPort.match(portString)) {
            retval.protocol = protocolWithoutPort.getParen(1).trim();
        } else {
            // Don't know what this is...
            log().error("Invalid service/port/protocol marker in the message: " + portString);
        }

        if (retval.isValid())
            return retval;
        else
            throw new IllegalArgumentException("String could not be parsed into a PortValues object");
    }

    /**
     * <p>parseDescr</p>
     *
     * @param descr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.vulnscand.DescrValues} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public DescrValues parseDescr(String descr) throws IllegalArgumentException {
        // Specific fields to be parsed out of the descr
        // Descr construction helpers
        String risk;

        DescrValues retval = new DescrValues();

        // Remove stray carriage returns
        descr = descr.replace('\n', ' ');

        // Locate useful information within the descr string

        // Get the risk factor
        if (riskFactor.match(descr)) {
            risk = riskFactor.getParen(2).trim();
	    String risk2 = descr;

            if (log().isDebugEnabled()) {
  	        log().debug("Descr Parsed: " + risk);
	        log().debug("Descr Parsed: " + risk2);
            }
	    
	    if (informational.match(risk)) {
                retval.severity = EventConstants.SEV_NORMAL;
            }
            if (normal.match(risk)) {
                retval.severity = EventConstants.SEV_NORMAL;
            }
            if (normal2.match(risk)) {
                retval.severity = EventConstants.SEV_NORMAL;
            }
            if (warning.match(risk)) {
                retval.severity = EventConstants.SEV_WARNING;
            }
            if (minor.match(risk)) {
                retval.severity = EventConstants.SEV_MINOR;
            }
            if (major.match(risk)) {
                retval.severity = EventConstants.SEV_MAJOR;
            }
            if (critical.match(risk)) {
                retval.severity = EventConstants.SEV_CRITICAL;
            }

            // If we could not locate a severity in the string...
            if (retval.severity == 0) {
                // Set it to be indeterminate
                retval.severity = EventConstants.SEV_INDETERMINATE;
            }

            // Clear the severity line so that
            // it can be added back later with some nicer
            // formatting
            descr = riskFactor.subst(descr, "");
        } else {
            risk = null;
            retval.severity = EventConstants.SEV_INDETERMINATE;
        }

        // Get the CVE string
        if (cveString.match(descr)) {
            retval.cveEntry = cveString.getParen(1).trim();

            // Validate the CVE ID number
            if (!cveId.match(retval.cveEntry)) {
                retval.cveEntry = null;
            }
            // Clear the CVE line so that it can be
            // added back later with some nicer
            // formatting
            else {
                descr = cveString.subst(descr, "");
            }
        } else {
            retval.cveEntry = null;
        }

        // Formatting/beautification changes

        // Remove multiple asterisks
        descr = multipleAsterisks.subst(descr, " ");

        // Change any markup in the text to string tokens
        // that will later be changed to HTML entities
        // (we can't change them to entities directly
        // because ';' is a reserved character in Nessus
        // messages).
        descr = greaterThan.subst(descr, GREATER_THAN_TOKEN);
        descr = lessThan.subst(descr, LESS_THAN_TOKEN);

        // Remove Nessus branding from messages
        descr = nessusLine.subst(descr, "The scanner");
        descr = nessusSentence.subst(descr, ". The scanner");
        descr = nessusTag.subst(descr, "> The scanner");
        descr = nessusInside.subst(descr, "the scanner");

        // Change semicolons into line breaks
        // descr = semicolonMulti.subst(descr, "<br /><br />");

        // Erase single semicolons
        // descr = semicolonSingle.subst(descr, " ");
        descr = semicolonSingle.subst(descr, "<br />");

        // Put periods before each line break
        /*
         * if (noPeriod.match(descr)) { descr = noPeriod.subst(descr,
         * noPeriod.getParen(1) + ". <br /> "); }
         */

        // Remove any trailing break tags
        descr = tooManyBreaks.subst(descr, "<br /><br />");
        descr = leadingOrTrailingBreaks.subst(descr, "");

        // Add an emphasized Risk Factor message
        if (risk != null) {
            descr = descr + "<br /><br />Risk Factor: <b>" + risk + "</b>";
        }

        // Add an emphasized Risk Factor message
        if (retval.cveEntry != null) {
            descr = descr + "<br /><br />Corresponding CVE Entry: <b>" + retval.cveEntry + "</b>";
        }

        // Replace the HTML tag tokens with HTML entities
        descr = greaterThanToken.subst(descr, GREATER_THAN_ENTITY);
        descr = lessThanToken.subst(descr, LESS_THAN_ENTITY);

        // Set the return value's description
        retval.descr = descr.trim();

        if (retval.isValid())
            return retval;
        else
            throw new IllegalArgumentException("String could not be parsed into a DescrValues object");
    }

    /**
     * <p>getDefaultDescrValues</p>
     *
     * @return a {@link org.opennms.netmgt.vulnscand.DescrValues} object.
     */
    public static DescrValues getDefaultDescrValues() {
        DescrValues retval = new DescrValues();
        retval.useDefaults();
        return retval;
    }

    /**
     * <p>getDefaultPortValues</p>
     *
     * @return a {@link org.opennms.netmgt.vulnscand.PortValues} object.
     */
    public static PortValues getDefaultPortValues() {
        PortValues retval = new PortValues();
        retval.useDefaults();
        return retval;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
