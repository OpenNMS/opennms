<%--

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
// 2008 Oct 04: Severity -> OnmsSeverity name change, eliminate severities variable. - dj@opennms.org
// 2008 Sep 27: Use new Severity enum. - dj@opennms.org
// 2006 Aug 15: HTML fixes from bug #1558. - dj@opennms.org
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		java.text.DecimalFormat,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.alarm.*,
                org.opennms.netmgt.model.OnmsSeverity
		"
%>
<%!
    public static final DecimalFormat MINUTE_FORMAT = new DecimalFormat( "00" );
%>
<%
    //get the service names, in alpha order
    Map serviceNameMap = new TreeMap(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap());
    Set serviceNameSet = serviceNameMap.keySet();
    Iterator serviceNameIterator = serviceNameSet.iterator();
 
    //get the current time values
    Calendar now = Calendar.getInstance();
    int nowHour = now.get(Calendar.HOUR); //gets the hour as a value between 1-12
    int nowMinute = now.get(Calendar.MINUTE);
    int nowAmPm = now.get(Calendar.AM_PM);
%>

<form action="alarm/query" method="get">
  <table width="100%" border="0" cellpadding="2" cellspacing="0">
    <tr>
      <td valign="top">
        <table width="100%" border="0" cellpadding="2" cellspacing="0" >
          <tr>
            <td>Alarm Text Contains:</td>
            <td>TCP/IP Address Like:</td>
          </tr>

          <tr>
            <td><input type="text" name="msgsub" /></td>
            <td><input type="text" name="iplike" value="" /></td>
          </tr>

          <tr>
            <td>Node Label Contains:</td>
            <td>Severity:</td>
          </tr>

          <tr>
            <td><input type="text" name="nodenamelike" /></td>
            <td>
              <select name="severity" size="1">
                <option selected="selected"><%=AlarmUtil.ANY_SEVERITIES_OPTION%></option>

                <% for (OnmsSeverity severity : OnmsSeverity.values()) { %>
                  <option value="<%=severity.getId()%>">
                    <%=severity.getLabel()%>
                  </option>
                <% } %>
              </select>
            </td>
          </tr>

          <tr>
            <td colspan="2">Service:</td>
          </tr>
          <tr>
            <td colspan="2">
              <select name="service" size="1">
                <option selected><%=AlarmUtil.ANY_SERVICES_OPTION%></option>

                <% while( serviceNameIterator.hasNext() ) { %>
                  <% String name = (String)serviceNameIterator.next(); %>
                  <option value="<%=serviceNameMap.get(name)%>"><%=name%></option>
                <% } %>
              </select>
            </td>
          </tr>

          <tr><td colspan="2"><hr width=100% /></td></tr>

          <tr>
            <td valign="top">
              <input type="checkbox" name="useafterfirsteventtime" value="1"/>Alarm First Event After:
            </td>
            <td valign="top">
              <input type="checkbox" name="usebeforefirsteventtime" value="1"/>Alarm First Event Before:
            </td>
          </tr>
          <tr>
            <td>
              <select name="afterfirsteventtimehour" size="1">
                <% for( int i = 1; i < 13; i++ ) { %>
                  <option value="<%=i%>" <%=(nowHour==i) ? "selected" : ""%>>
                    <%=i%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="afterfirsteventtimeminute" size="4" maxlength="2" value="<%=MINUTE_FORMAT.format(nowMinute)%>" />

              <select name="afterfirsteventtimeampm" size="1">
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour != 12) ? "selected" : ""%>>AM</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour == 12) ? "selected" : ""%>>Noon</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour != 12) ? "selected" : ""%>>PM</option>
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour == 12) ? "selected" : ""%>>Midnight</option>
              </select>
            </td>
            <td>
              <select name="beforefirsteventtimehour" size="1">
                <% for( int i = 1; i < 13; i++ ) { %>
                  <option value="<%=i%>" <%=(nowHour==i) ? "selected=\"selected\"" : ""%>>
                    <%=i%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="beforefirsteventtimeminute" size="4" maxlength="2" value="<%=MINUTE_FORMAT.format(nowMinute)%>" />

              <select name="beforefirsteventtimeampm" size="1">
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour != 12) ? "selected" : ""%>>AM</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour == 12) ? "selected" : ""%>>Noon</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour != 12) ? "selected" : ""%>>PM</option>
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour == 12) ? "selected" : ""%>>Midnight</option>
              </select>
            </td>
          </tr>
          <tr>
            <td>
              <select name="afterfirsteventtimemonth" size="1">
                <% for( int i = 0; i < 12; i++ ) { %>
                  <option value="<%=i%>" <%=(now.get(Calendar.MONTH)==i) ? "selected" : ""%>>
                    <%=months[i]%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="afterfirsteventtimedate" size="4" maxlength="2" value="<%=now.get(Calendar.DATE)%>" />
              <input type="text" name="afterfirsteventtimeyear" size="6" maxlength="4" value="<%=now.get(Calendar.YEAR)%>" />
            </td>
            <td>
              <select name="beforefirsteventtimemonth" size="1">
                <% for( int i = 0; i < 12; i++ ) { %>
                  <option value="<%=i%>" <%=(now.get(Calendar.MONTH)==i) ? "selected" : ""%>>
                    <%=months[i]%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="beforefirsteventtimedate" size="4" maxlength="2" value="<%=now.get(Calendar.DATE)%>" />
              <input type="text" name="beforefirsteventtimeyear" size="6" maxlength="4" value="<%=now.get(Calendar.YEAR)%>" />
            </td>
          </tr>

          <tr>
            <td valign="top">
              <input type="checkbox" name="useafterlasteventtime" value="1"/>Alarm Last Event After:
            </td>
            <td valign="top">
              <input type="checkbox" name="usebeforelasteventtime" value="1"/>Alarm Last Event Before:
            </td>
          </tr>
          <tr>
            <td>
              <select name="afterlasteventtimehour" size="1">
                <% for( int i = 1; i < 13; i++ ) { %>
                  <option value="<%=i%>" <%=(nowHour==i) ? "selected" : ""%>>
                    <%=i%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="afterlasteventtimeminute" size="4" maxlength="2" value="<%=MINUTE_FORMAT.format(nowMinute)%>" />

              <select name="afterlasteventtimeampm" size="1">
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour != 12) ? "selected" : ""%>>AM</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour == 12) ? "selected" : ""%>>Noon</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour != 12) ? "selected" : ""%>>PM</option>
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour == 12) ? "selected" : ""%>>Midnight</option>
              </select>
            </td>
            <td>
              <select name="beforelasteventtimehour" size="1">
                <% for( int i = 1; i < 13; i++ ) { %>
                  <option value="<%=i%>" <%=(nowHour==i) ? "selected=\"selected\"" : ""%>>
                    <%=i%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="beforelasteventtimeminute" size="4" maxlength="2" value="<%=MINUTE_FORMAT.format(nowMinute)%>" />

              <select name="beforelasteventtimeampm" size="1">
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour != 12) ? "selected" : ""%>>AM</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour == 12) ? "selected" : ""%>>Noon</option>
                <option value="pm" <%=(nowAmPm == Calendar.PM && nowHour != 12) ? "selected" : ""%>>PM</option>
                <option value="am" <%=(nowAmPm == Calendar.AM && nowHour == 12) ? "selected" : ""%>>Midnight</option>
              </select>
            </td>
          </tr>
          <tr>
            <td>
              <select name="afterlasteventtimemonth" size="1">
                <% for( int i = 0; i < 12; i++ ) { %>
                  <option value="<%=i%>" <%=(now.get(Calendar.MONTH)==i) ? "selected" : ""%>>
                    <%=months[i]%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="afterlasteventtimedate" size="4" maxlength="2" value="<%=now.get(Calendar.DATE)%>" />
              <input type="text" name="afterlasteventtimeyear" size="6" maxlength="4" value="<%=now.get(Calendar.YEAR)%>" />
            </td>
            <td>
              <select name="beforelasteventtimemonth" size="1">
                <% for( int i = 0; i < 12; i++ ) { %>
                  <option value="<%=i%>" <%=(now.get(Calendar.MONTH)==i) ? "selected" : ""%>>
                    <%=months[i]%>
                  </option>
                <% } %>
              </select>

              <input type="text" name="beforelasteventtimedate" size="4" maxlength="2" value="<%=now.get(Calendar.DATE)%>" />
              <input type="text" name="beforelasteventtimeyear" size="6" maxlength="4" value="<%=now.get(Calendar.YEAR)%>" />
            </td>
          </tr>

          <tr><td colspan="2"><hr width=100% /></td></tr>

          <tr>
            <td>Sort By:</td>
            <td>Number of Alarms Per Page:</td>
          </tr>
          <tr>
            <td>
              <select name="sortby" size="1">
                <option value="id"               >Alarm ID  (Descending)</option>
                <option value="rev_id"           >Alarm ID  (Ascending) </option>
                <option value="severity"         >Severity  (Descending)</option>
                <option value="rev_severity"     >Severity  (Ascending) </option>
                <option value="lasteventtime"    >Time      (Descending)</option>
                <option value="rev_lasteventtime">Time      (Ascending) </option>
                <option value="node"             >Node      (Ascending) </option>
                <option value="rev_node"         >Node      (Descending)</option>
                <option value="interface"        >Interface (Ascending) </option>
                <option value="rev_interface"    >Interface (Descending)</option>
                <option value="service"          >Service   (Ascending) </option>
                <option value="rev_service"      >Service   (Descending)</option>
              </select>
            </td>
            <td>
              <select name="limit" size="1">
                <option value="10">10 alarms</option>
                <option value="20">20 alarms</option>
                <option value="30">30 alarms</option>
                <option value="50">50 alarms</option>
                <option value="100">100 alarms</option>
                <option value="-1">all alarms</option>
              </select>
            </td>
          </tr>

          <tr><td colspan="2"><hr width=100% /></td></tr>

        </table>
      </td>
    </tr>
    <tr>
      <td>
        <input type="submit" value="Search" />
      </td>
    </tr>
  </table>
</form>


<%!
    public static String getAmPm( int hour ) {
        switch(hour) {
            case 24:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return "AM";
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                return "PM";
            default:
                throw new IllegalArgumentException("Can only take hours 1-24, " + hour + " is illegal");
        }
    }

    //note these run from 0-11, this is because of java.util.Calendar!
    public static String[] months = new String[] {
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    };
%>


