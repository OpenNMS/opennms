<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
	import="
		java.io.*,
		java.util.*, java.util.Map.Entry,
		java.net.InetAddress, java.net.UnknownHostException,
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.notifications.*,
		org.opennms.core.utils.BundleLists,
		org.opennms.core.utils.ConfigFileConstants,
		org.opennms.netmgt.xml.eventconf.Event,
		org.springframework.core.io.FileSystemResource
	"
%>
<%!
	private DefaultEventConfDao m_eventConfDao;

	public void init() throws ServletException {
		try {
			m_eventConfDao = new DefaultEventConfDao();
			m_eventConfDao.setConfigResource(new FileSystemResource(ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME)));
			m_eventConfDao.afterPropertiesSet();
		} catch (Throwable e) {
			throw new ServletException("Cannot load configuration file", e);
		}
	}
%>
<%
    HttpSession user = request.getSession(true);

    String hostName = "localhost";
    try {
	java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
        hostName = localMachine.getHostName();
    } catch(java.net.UnknownHostException uhe) {
	//handle exception
    }
%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Send Event" />
  <jsp:param name="headTitle" value="Send Event" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Send Event" />
  <jsp:param name="link" value='<link type="text/css" href="js/jquery/themes/base/jquery.ui.all.css" rel="stylesheet" />' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/jquery/jquery.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/jquery/ui/jquery.ui.core.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/jquery/ui/jquery.ui.widget.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/jquery/ui/jquery.ui.button.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/jquery/ui/jquery.ui.position.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/jquery/ui/jquery.ui.autocomplete.js"></script>' />
</jsp:include>
<style type="text/css">
        /* TODO shouldn't be necessary */
        .ui-button { margin-left: -1px; }
        .ui-button-icon-only .ui-button-text { padding: 0em; }
        .ui-autocomplete-input { margin: 0; padding: 0.12em 0 0.12em 0.20em; }
</style>

<script type="text/javascript" >

    function next()
    {
        if (document.getElementById("input.uei").selectedIndex==0)
        {
            alert("Please select a uei to associate with this event.");
        }
        else
        {
            document.getElementById("form.sendevent").submit();
        }
    }

    // Invoke a jQuery function
    $(document).ready(function() {
        // Create the 'combobox' widget which can widgetize a <select> tag
        $.widget("ui.combobox", {
            _create: function() {
                var self = this;
                // Hide the existing tag
                var select = this.element.hide();
                // Add an autocomplete text field
                var input = $("<input name=\"" + self.options.name + "\">")
                                .insertAfter(select)
                                .autocomplete({
                                    source: self.options.jsonUrl,
                                    delay: 1000,
                                    change: function(event, ui) {
                                        if (!ui.item) {
                                            // remove invalid value, as it didn't match anything
                                            $(this).val("");
                                            return false;
                                        }
                                        select.val(ui.item.id);
                                        self._trigger("selected", event, {
                                            item: select.find("[value='" + ui.item.id + "']")
                                        });

                                    },
                                    blur: function(event, ui) {
                                        if (this("widget").is(":visible")) {
                                            this("close");
                                            return;
                                        }
                                    },
                                    minLength: 0
                                })
                                .addClass("ui-widget ui-widget-content ui-corner-left");

                // Add a dropdown arrow button that will expand the entire list
                $("<button type=\"button\">&nbsp;</button>")
                    .attr("tabIndex", -1)
                    .attr("title", "Show All Items")
                    .insertAfter(input)
                    .button({
                        icons: {
                            primary: "ui-icon-triangle-1-s"
                        },
                        text: false
                    })
                    .removeClass("ui-corner-all")
                    .addClass("ui-corner-right ui-button-icon")
                    .click(function() {
                        // close if already visible
                        if (input.autocomplete("widget").is(":visible")) {
                            input.autocomplete("close");
                            return;
                        }
                        // pass empty string as value to search for, displaying all results
                        input.autocomplete("search", "");
                        input.focus();
                    });
            }
        });

        $("#nodeSelect").combobox({name:"nodeid", jsonUrl: "admin/sched-outages/jsonNodes.jsp"});
        $("#interfaceSelect").combobox({name: "interface", jsonUrl: "admin/sched-outages/jsonIpInterfaces.jsp"});

        $('#addparm').click(function(event) {
          event.preventDefault();
          var lastNum = 0;
          $('#parmlist > div').each(function(index,el) {
            var num = Number( $(el).attr('id').substring(4) );
            if (lastNum <= num) { lastNum = num + 1; }
          });
          var nextParm = 'parm' + lastNum;
          $('<div id="' + nextParm + '"></div>')
            .append(
                $('<input type="image" src="images/redcross.gif" />')
                  .attr('id', nextParm+'.delete')
                  .attr('name', nextParm+'.delete')
                  .click(function() {
                    $(this).parent().remove();
                  })
              , ' ')
            .append('Name: <input id="'+nextParm+'.name" name="'+nextParm+'.name" type="text" value="" /> ')
            .append('Value: <textarea id="'+nextParm+'.value" name="'+nextParm+'.value" cols="60" rows="1"></textarea>')
          .appendTo($('#parmlist'));
        });
    });

</script>

<h3>Send Event to OpenNMS</h3>

<form method="post" name="sendevent"
      id="form.sendevent"
      action="admin/postevent.jsp" >
      <table width="50%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td valign="top" align="left">
            <h4>Events</h4>
            <select NAME="uei" SIZE="1" id="input.uei" >
               <option value="">--Select One--</option>
               <%=buildEventSelect()%>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <div class="ui-widget">
              <label>Node ID:</label>
              <select id="nodeSelect" name="nodeSelect" style="display: none"></select>
            </div>
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <label>Source Hostname:</label>
            <input id="hostname" name="hostname" type="text" value="<%=hostName%>" />
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <div class="ui-widget">
              <label>Interface:</label>
              <select id="interfaceSelect" name="interfaceSelect" style="display: none"></select>
            </div>
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <label>Service:</label>
            <input id="service" name="service" type="text" value="" />
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <label>Parameters</label>
            <div id="parmlist"></div>
            <br/>
            <a id="addparm">Add additional parameter</a>
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <h4>Description</h4>
            <textarea id="description" name="description" cols="60" rows="5" style="resize: none;"></textarea>
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <h4>Severity: </h4>
            <select NAME="severity" SIZE="1" >
              <option value="">--Select One--</option>
              <option>Indeterminate</option>
              <option>Cleared</option>
              <option>Normal</option>
              <option>Warning</option>
              <option>Minor</option>
              <option>Major</option>
              <option>Critical</option>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
            <h4>Operator Instructions</h4>
            <textarea id="operinstruct" name="operinstruct" cols="60" rows="5" style="resize: none;"></textarea>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="reset"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a href="javascript:next()">Send Event &#155;&#155;&#155;</a>
          </td>
        </tr>
      </table>
    </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    public String buildEventSelect()
      throws IOException, FileNotFoundException
    {
        List events = m_eventConfDao.getEventsByLabel();
        StringBuffer buffer = new StringBuffer();

        List<String> excludeList = getExcludeList();
	TreeMap<String, String> sortedMap = new TreeMap<String, String>();

        Iterator i = events.iterator();

        while(i.hasNext()) {
            org.opennms.netmgt.xml.eventconf.Event e = (org.opennms.netmgt.xml.eventconf.Event)i.next();

            String uei = e.getUei();
            //System.out.println(uei);

            String label = e.getEventLabel();
            //System.out.println(label);

            String trimmedUei = stripUei(uei);
            //System.out.println(trimmedUei);

            if (!excludeList.contains(trimmedUei)) {
		sortedMap.put(label,uei);
                //System.out.println("sortedMap.put('"+label+"', '"+uei+"')");
            }
	}
        for(Map.Entry<String, String> me : sortedMap.entrySet()) {
            buffer.append("<option value=" + me.getValue() + ">" + me.getKey() + "</option>");
        }

        return buffer.toString();
    }

    public String stripUei(String uei)
    {
        String leftover = uei;

        for (int i = 0; i < 3; i++)
        {
            leftover = leftover.substring(leftover.indexOf('/')+1);
        }

        return leftover;
     }

     public List<String> getExcludeList()
      throws IOException, FileNotFoundException
     {
        List<String> excludes = new ArrayList<String>();

        Properties excludeProperties = new Properties();
	excludeProperties.load( new FileInputStream( ConfigFileConstants.getFile(ConfigFileConstants.EXCLUDE_UEI_FILE_NAME )));
        String[] ueis = BundleLists.parseBundleList( excludeProperties.getProperty( "excludes" ));

        for (int i = 0; i < ueis.length; i++)
        {
            excludes.add(ueis[i]);
        }

        return excludes;
     }
%>
