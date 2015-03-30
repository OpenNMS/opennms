<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>
<%@page import="java.util.*"%>
<%@page import="java.text.*"%>
<%@page import="org.opennms.netmgt.config.*"%>
<%@page import="org.opennms.netmgt.config.users.*"%>
<%@page import="org.opennms.web.api.Util" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%

        final HttpSession userSession = request.getSession(false);
        User user = null;
        String userid = "";
        UserManager userFactory;
        try {
            UserFactory.init();
            userFactory = UserFactory.getInstance();
        } catch (Throwable e) {
            throw new ServletException("UserFactory:modify() " + e);
        }

        if (userSession != null) {
            user = (User) userSession.getAttribute("user.modifyUser.jsp");
            userid = user.getUserId();
        }
%>
<c:set var="baseHref" value="<%=Util.calculateUrlBase(request)%>"/>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Modify User" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Users" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}admin/userGroupView/users/list.jsp'>User List</a>" />
  <jsp:param name="breadcrumb" value="Modify User" />
</jsp:include>

<script type="text/javascript" >
    
    function validate()
    {
        var minDurationMinsWarning = 5;
        var warnMinDuration = true;

        for (var c = 0; c < document.modifyUser.dutySchedules.value; c++)
        {
            var beginName= "duty" + c + "Begin";
            var endName  = "duty" + c + "End";
            
            var beginValue = new Number(document.modifyUser.elements[beginName].value);
            var endValue = new Number(document.modifyUser.elements[endName].value);

            var beginHour = Math.floor(beginValue / 100), endHour = Math.floor(endValue / 100);
            var beginMin = beginValue % 100, endMin = endValue % 100
            var duration = ((endHour * 60) + endMin) - ((beginHour * 60) + beginMin);

            if (!document.modifyUser.elements["deleteDuty"+c].checked)
            {
            if (isNaN(beginValue))
            {
                alert("The begin time of duty schedule " + (c+1) + " must be expressed in military time with no other characters, such as 800, not 8:00");
                return false;
            }
            if (isNaN(endValue))
            {
                alert("The end time of duty schedule " + (c+1) + " must be expressed in military time with no other characters, such as 800, not 8:00");
                return false;
            }
            if (beginValue > endValue)
            {
                alert("The begin value for duty schedule " + (c+1) + " must be less than the end value.");
                return false;
            }
            if (beginValue < 0 || beginValue > 2359)
            {
                alert("The begin value for duty schedule " + (c+1) + " must be greater than 0 and less than 2400");
                return false;
            }
            if (endValue < 0 || endValue > 2359)
            {
                alert("The end value for duty schedule " + (c+1) + " must be greater than 0 and less than 2400");
                return false;
            }
            if ((duration <= minDurationMinsWarning) && warnMinDuration)
            {
                if (!(confirm("Warning: One or more duty schedules are unusually short in duration (" + minDurationMinsWarning + " minutes or less)\n\nSave these schedules?")))
                    return false;
                else
                    warnMinDuration = false;    // only once
            }
            }
        }
        return true;
    }

    function resetPassword()
    {
        newUserWin = window.open("<%= Util.calculateUrlBase(request, "admin/userGroupView/users/newPassword.jsp") %>", "", "fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no,width=500,height=300");
    }

    function addDutySchedules()
    {
        var ok = validate();

        if(ok)
        {
          document.modifyUser.redirect.value="/admin/userGroupView/users/addDutySchedules";
          document.modifyUser.action="<%= Util.calculateUrlBase(request, "admin/userGroupView/users/updateUser") %>";
          document.modifyUser.submit();
        }
    }
    
    function removeDutySchedules()
    {
        var ok = validate();
        
        if(ok)
        {
          document.modifyUser.redirect.value="/admin/userGroupView/users/modifyUser.jsp";
          document.modifyUser.action="<%= Util.calculateUrlBase(request, "admin/userGroupView/users/updateUser") %>";
          document.modifyUser.submit();
        }
    }
    
    function saveUser()
    {
        var ok = validate();

        if(ok)
        {
          document.modifyUser.redirect.value="/admin/userGroupView/users/saveUser";
          document.modifyUser.action="<%= Util.calculateUrlBase(request, "admin/userGroupView/users/updateUser") %>";
          document.modifyUser.submit();
        }
        else
          document.modifyUser.redirect.value="/admin/userGroupView/users/modifyUser.jsp";
    }
    
    function cancelUser()
    {
        document.modifyUser.action="<%= Util.calculateUrlBase(request, "admin/userGroupView/users/list.jsp") %>";
        document.modifyUser.submit();
    }
</script>

<form role="form" class="form-horizontal" id="modifyUser" method="post" name="modifyUser">
<input id="userID" type="hidden" name="userID" value="<%=user.getUserId()%>"/>
<input id="password" type="hidden" name="password"/>
<input id="redirect" type="hidden" name="redirect"/>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Modify User: <%=userid%></h3>
      </div>
      <div class="panel-body">
        <h3>User Password</h3>
        <div class="col-sm-10 col-sm-offset-2">
          <button type="button" class="btn btn-default" onClick="resetPassword()">Reset Password</button>
        </div>

        <h3>User Information</h3>
	<%
	String tuiPin = null;
	String email = null;
        String pagerEmail = null;
        String xmppAddress = null;
        String numericPage = null;
        String numericPin = null;
        String textPage = null;
        String textPin = null;
        String workPhone = null;
        String mobilePhone = null;
        String homePhone = null;
        String microblog = null;
        String fullName = null;
        String comments = null;
        Boolean isReadOnly = false;
        try {
            User usertemp = userFactory.getUser(userid);
            if (usertemp != null) {
                    email = userFactory.getEmail(userid);
                    pagerEmail = userFactory.getPagerEmail(userid);
                    xmppAddress = userFactory.getXMPPAddress(userid);
                    numericPage = userFactory.getNumericPage(userid);
                    numericPin = userFactory.getNumericPin(userid);
                    textPage = userFactory.getTextPage(userid);
                    textPin = userFactory.getTextPin(userid);
                    workPhone = userFactory.getWorkPhone(userid);
                    mobilePhone = userFactory.getMobilePhone(userid);
                    homePhone = userFactory.getHomePhone(userid);
                    microblog = userFactory.getMicroblogName(userid);
            } else {
                    Contact[] contact = user.getContact();
                    for (int i = 0; i < contact.length; i++) {
                            if (contact[i].getType().equals("email")) {
                                    email = contact[i].getInfo();
                            } else if (contact[i].getType().equals("pagerEmail")) {
                                    pagerEmail = contact[i].getInfo();
                            } else if (contact[i].getType().equals("xmppAddress")) {
                                    xmppAddress = contact[i].getInfo();
                            } else if (contact[i].getType().equals("numericPage")) {
                                    numericPage = contact[i].getInfo();
                            } else if (contact[i].getType().equals("textPage")) {
                                    textPage = contact[i].getInfo();
                            } else if (contact[i].getType().equals("workPhone")) {
                                    workPhone = contact[i].getInfo();
                            } else if (contact[i].getType().equals("mobilePhone")) {
                                    mobilePhone = contact[i].getInfo();
                            } else if (contact[i].getType().equals("homePhone")) {
                                    homePhone = contact[i].getInfo();
                            } else if (contact[i].getType().equals("microblog")) {
                            		microblog = contact[i].getInfo();
                            }
                    }
            }
            fullName = user.getFullName();
            comments = user.getUserComments();
            tuiPin = user.getTuiPin();
            isReadOnly = user.isReadOnly();
        } catch (org.exolab.castor.xml.MarshalException e) {
            throw new ServletException("An Error occurred reading the users file", e);
        } catch (org.exolab.castor.xml.ValidationException e) {
            throw new ServletException("An Error occurred reading the users file", e);
        }

        %>

	<div class="form-group">
          <label for="fullName" class="col-sm-2 control-label">Full Name:</label>
          <div class="col-sm-10">
            <input id="fullName" type="text" class="form-control" size="35" name="fullName" value="<%=(fullName == null ? "":fullName) %>" />
          </div>
        </div>

	<div class="form-group">
          <label for="userComments" class="col-sm-2 control-label">Comments:</label>
          <div class="col-sm-10">
            <textarea class="form-control" rows="5" id="userComments" name="userComments"><%=(comments == null ? "" : comments)%></textarea>
          </div>
        </div>

	<div class="form-group">
          <label for="tuiPin" class="col-sm-2 control-label">Telephone PIN:</label>
          <div class="col-sm-10">
            <input class="form-control" id="tuiPin" type="text" name="tuiPin" value="<%=(tuiPin == null ? "" : tuiPin)%>" />
          </div>
        </div>

        <h3>Notification Information</h3>

	<div class="form-group">
          <label for="email" class="col-sm-2 control-label">Email:</label>
          <div class="col-sm-10">
            <input class="form-control" id="email" type="text" name="email" value='<%= (email == null ? "":email) %>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="pemail" class="col-sm-2 control-label">Pager Email:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="pemail" name="pemail" value='<%=(pagerEmail == null ? "":pagerEmail)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="xmppAddress" class="col-sm-2 control-label">XMPP Address:</label>
          <div class="col-sm-10">
            <input class="form-control" id="xmppAddress" type="text" name="xmppAddress" value='<%=(xmppAddress == null ? "":xmppAddress)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="microblog" class="col-sm-2 control-label">Microblog Username:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="microblog" name="microblog" value='<%=(microblog == null ? "":microblog)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="numericalService" class="col-sm-2 control-label">Numeric Service:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="numericalService" name="numericalService" value='<%=(numericPage == null ? "":numericPage) %>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="numericalPin" class="col-sm-2 control-label">Numeric PIN:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="numericalPin" name="numericalPin" value='<%= (numericPin == null ? "":numericPin)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="textService" class="col-sm-2 control-label">Text Service:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="textService" name="textService" value='<%= (textPage == null ? "":textPage)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="textPin" class="col-sm-2 control-label">Text PIN:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="textPin" name="textPin" value='<%=(textPin == null ? "":textPin)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="workPhone" class="col-sm-2 control-label">Work Phone:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="workPhone" name="workPhone" value='<%=(workPhone == null ? "":workPhone)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="mobilePhone" class="col-sm-2 control-label">Mobile Phone:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="mobilePhone" name="mobilePhone" value='<%=(mobilePhone == null ? "":mobilePhone)%>'/>
          </div>
        </div>

	<div class="form-group">
          <label for="homePhone" class="col-sm-2 control-label">Home Phone:</label>
          <div class="col-sm-10">
            <input class="form-control" type="text" id="homePhone" name="homePhone" value='<%=(homePhone == null ? "":homePhone)%>'/>
          </div>
        </div>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">User Properties</h3>
      </div>
      <div class="panel-body">
        <p>
          This panel allows you to modify information for each user, including
          their name, notification information, and duty schedules.
        </p>

        <p>
          <b>Notification Information</b> provides the ability for you to configure
          contact information for each user, including any of <em>email</em>
          address, <em>pager email</em> (in the case that the pager can be reached
          as an email destination), <em>XMPP address</em> (for instant messages
          using the Jabber XMPP protocol), <em>numeric service</em> (for pagers
          that cannot display text messages), <em>text service</em> (for
          alphanumeric pagers), and <em>work phone</em>, <em>mobile phone</em>, and
          <em>home phone</em> for notifications by telephone. The <em>Telephone
          PIN</em> is an optional numeric field used to authenticate called users.
        </p>

        <p>
          <b>Duty Schedules</b> allow you to flexibility to determine when users
          should receive notifications.  A duty schedule consists of a list of
          days for which the time will apply and a time range, presented in
          military time with no punctuation.  Using this standard, days run from
          <em>0000</em> to <em>2359</em>.
        </p>

        <p>
          If your duty schedules span midnight, or if your users work multiple,
          non-contiguous time periods, you will need to configure multiple duty
          schedules.  To do so, select the number of duty schedules to add from
          the drop-down box next to <b>[Add This Many Schedules]</b>, and click
          the button.  Then, using the duty schedule fields you've just added,
          create a duty schedule from the start time to 2359 on one day, and
          enter a second duty schedule which begins at 0000 and ends at the end
          of that users coverage.
        </p>

        <p>
          To remove configured duty schedules, put a check in the <em>Delete</em>
          column and click <b>[Remove Checked Schedules]</b>.
        </p>

        <p>
         To save your configuration, click on <b>[Finish]</b>.
        </p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<%
Collection<String> dutySchedules = user.getDutyScheduleCollection();
%>
<input type="hidden" name="dutySchedules" value="<%=user.getDutyScheduleCount()%>"/>

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Duty Schedule</h3>
      </div>
      <table class="table table-condensed table-striped table-bordered">
        <thead>
          <tr>
          <th>&nbsp;</th>
          <th>Delete</th>
          <th>Mo</th>
          <th>Tu</th>
          <th>We</th>
          <th>Th</th>
          <th>Fr</th>
          <th>Sa</th>
          <th>Su</th>
          <th>Begin Time</th>
          <th>End Time</th>
          </tr>
        </thead>
        <%
        int i = 0;
        for (String dutySchedule : dutySchedules) {
            DutySchedule tmp = new DutySchedule(dutySchedule);
            Vector<Object> curSched = tmp.getAsVector();
            %>
        <tbody>
                        <tr>
                          <td width="1%"><%=(i + 1)%></td>
                          <td width="1%">
                            <input type="checkbox" name="deleteDuty<%=i%>"/>
                          </td>
                          <%ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");
            for (int j = 0; j < 7; j++) {
                Boolean curDay = (Boolean) curSched.get(j);

                %>
                          <td width="5%">
                            <input type="checkbox" name="duty<%=i+days.format(j)%>" <%= (curDay.booleanValue() ? "checked=\"true\"" : "")%>/>
                          </td>
                          <%}
            %>
                          <td width="5%">
                            <input type="text" class="form-control" size="4" name="duty<%=i%>Begin" value="<%=curSched.get(7)%>"/>
                          </td>
                          <td width="5%">
                            <input type="text" class="form-control" size="4" name="duty<%=i%>End" value="<%=curSched.get(8)%>"/>
                          </td>
                        </tr>
                        <%i++;
        }
        %>
         </tbody>
       </table>

       <div class="form-group top-buffer">
         <div class="col-sm-12">
           <button id="addSchedulesButton" type="button" class="btn btn-default" name="addSchedule" onclick="addDutySchedules()">Add This Many Schedules</button>
           <select name="numSchedules" class="btn btn-default" value="3">
                 <option value="1">1</option>
                 <option value="2">2</option>
                 <option value="3">3</option>
                 <option value="4">4</option>
                 <option value="5">5</option>
                 <option value="6">6</option>
                 <option value="7">7</option>
           </select>
         </div>
       </div>

       <div class="form-group">
         <div class="col-sm-12">
           <button id="removeSchedulesButton" type="button" class="btn btn-default" name="addSchedule" onclick="removeDutySchedules()">Remove Checked Schedules</button>
         </div>
       </div>
   </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <button id="saveUserButton" type="submit" class="btn btn-default" name="finish" onclick="saveUser()">Finish</button>
    <button id="cancelButton" type="button" class="btn btn-default" name="cancel" onclick="cancelUser()">Cancel</button>
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
