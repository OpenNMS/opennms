<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
	import="java.util.*,
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.destinationPaths.*
	"
%>

<%!
    public void init() throws ServletException {
        try {
            DestinationPathFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    String intervals[] = {"0s", "1s","2s","5s","10s","15s","30s","0m", "1m", "2m", "5m", "10m", "15m", "30m", "1h", "2h", "3h", "6h", "12h", "1d"};
    HttpSession user = request.getSession(true);
    Path newPath = (Path)user.getAttribute("newPath");
    List<String> targetLinks = new ArrayList<String>();
    List<String> escalateDelays = new ArrayList<String>();
    
    targetLinks.add( "Initial Targets" );
    final List<Escalate> escalates = newPath.getEscalates();
    String[] targets = new String[escalates.size()];
    for (int i = 0; i < targets.length; i++)
    {
        targetLinks.add("Escalation # " + (i+1));
        escalateDelays.add(escalates.get(i).getDelay());
    }
%>

<script type="text/javascript" >

    function edit_path(index) 
    {
        document.outline.userAction.value="edit";
        document.outline.index.value=index;
        document.outline.submit();
    }
    
    function add_path(index)
    {
        document.outline.userAction.value="add";
        document.outline.index.value=index;
        document.outline.submit();
    }
    
    function remove_path(index)
    {
        message = "Are you sure you want to remove escalation #" + (index+1);
        if (confirm(message))
        {
            document.outline.userAction.value="remove";
            document.outline.index.value=index;
            document.outline.submit();
        }
    }
    
    function trimString(str) 
    {
        while (str.charAt(0)==" ")
        {
          str = str.substring(1);
        }
        while (str.charAt(str.length - 1)==" ")
        {
          str = str.substring(0, str.length - 1);
        }
        return str;
    }
    
    function finish()
    {
        trimmed = trimString(document.outline.name.value);
        if (trimmed=="")
        {
            alert("Please give this path a name.");
            return false;
        }
        else if (trimmed.indexOf(" ") != -1)
        {
            alert("Please do not use spaces in path names.");
            return false;
        }
        else if (document.outline.escalate0.options.length==0)
        {
            alert("Please give this path some initial targets.");
            return false;
        }
        else
        {
            document.outline.userAction.value="finish";
            return true;
        }
    }
    
    function cancel()
    {
        document.outline.userAction.value="cancel";
        document.outline.submit();
    }

</script>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Path Outline" />
  <jsp:param name="headTitle" value="Path Outline" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/destinationPaths.jsp'>Destination Paths</a>" />
  <jsp:param name="breadcrumb" value="Path Outline" />
</jsp:include>

<h2><%=(newPath.getName()!=null ? "Editing path: " + newPath.getName() + "<br/>" : "")%></h2>

<form role="form" class="form-horizontal" method="post" name="outline" action="admin/notification/destinationWizard" onsubmit="return finish();">
  <input type="hidden" name="sourcePage" value="pathOutline.jsp"/>
  <input type="hidden" name="index"/>
  <input type="hidden" name="userAction"/>
  <input type="hidden" name="escalation" value="false"/>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Choose the piece of the path that you want to edit from below. When
          all editing is complete click the <i>Finish</i> button. No changes will
          be permanent until the <i>Finish</i> button has been clicked.</h3>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <label for="input_name" class="control-label col-sm-2">Name:</label>
          <div class="col-sm-10">
            <% if (newPath.getName()==null) { %>
              <input type="text" class="form-control" name="name" value=""/>
            <% } else { %>
              <input type="text" class="form-control" name="name" value="<%=newPath.getName()%>"/>
            <% } %>
          </div>
        </div>
        <div class="form-group">
          <label for="input_initialDelay" class="control-label col-sm-2">Initial Delay:</label>
          <div class="col-sm-10">
            <%=buildDelaySelect(intervals, "initialDelay", newPath.getInitialDelay().orElse(null))%>
          </div>
        </div>
        <div class="form-group">
          <div class="col-sm-10 col-sm-offset-2">
      <table class="table table-condensed table-borderless">
        <% for (int i = 0; i < targetLinks.size(); i++) { %>
         <tr>
           <td>
            <% if (i!=0) { %>
            <hr>
            <% } %>
            <table class="table table-condensed table-borderless">
              <tr>
                <td width="10%">
                  <b>
                  <% if (i==0) { %>
                    <%="Initial Targets"%>
                  <% } else { %>
                    <%="Escalation #" + i%>
                  <% } %>
                  </b>
                  <br/>
                  <% if (i > 0) { %>
                    Delay:
                    <%=buildDelaySelect(intervals, "escalate"+(i-1)+"Delay", newPath.getEscalates().get(i-1).getDelay())%><br/>
                  <% } %>
                  <%=buildTargetList(i, newPath, "escalate"+i)%>
                </td>
                <td width="5%" valign="top">
                    <input type="button" class="btn btn-default" value="Edit" onclick="edit_path(<%=i-1%>)"/>
                    <br/>
                    &nbsp;
                    <br/>
                    <%if (i > 0) { %>
                      <input type="button" class="btn btn-default" value="Remove" onclick="remove_path(<%=i-1%>)"/>
                    <% } else { %>
                      &nbsp;
                    <% } %>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td>
            <input type="button" class="btn btn-default" value="Add Escalation" onclick="add_path(<%=i%>)"/>
          </td>
        </tr>
        <% } %>
      </table>
           </div> <!-- column -->
         </div> <!-- form-group -->
      </div> <!-- panel-body -->
      <div class="panel-footer">
        <input type="submit" class="btn btn-default" value="Finish"/>
        <input type="button" class="btn btn-default" value="Cancel" onclick="cancel()"/>
      </div> <!-- panel-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->


</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    public String buildDelaySelect(String[] intervals, String name, String currValue)
    {
          boolean gotCurrValue = false;
          StringBuffer buffer = new StringBuffer("<select class=\"form-control\" id=\"input_" + name + "\" name=\"" + name  + "\">");
                    
          for (int i = 0; i < intervals.length; i++)
          {
             if (intervals[i].equals(currValue))
             {
                 buffer.append("<option selected=\"selected\" value=\"" + intervals[i] + "\">").append(intervals[i]).append("</option>");
                 gotCurrValue = true;
             }
             else
             {
                  buffer.append("<option value=\"" + intervals[i] + "\">").append(intervals[i]).append("</option>");
             }
          }
          if (!gotCurrValue)
          {
              buffer.append("<option selected=\"selected\" value=\"" + currValue + "\">").append(currValue).append("</option>");
          }
          buffer.append("</select>");
          
          return buffer.toString();
    }
    
    public String buildTargetList(int index, Path path, String name)
    {
        StringBuffer buffer = new StringBuffer("<select class=\"form-control\" name=\""+name+"\" size=\"4\">");
        List<Target> targets = null;
        
        if (index == 0)
        {
            targets = path.getTargets();
        }
        else
        {
            targets = path.getEscalates().get(index-1).getTargets();
        }
        
        for (final Target t : targets) {
            buffer.append("<option>").append(t.getName()).append("</option>");
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }
%>
