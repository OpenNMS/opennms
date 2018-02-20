<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
	import="
		org.opennms.core.utils.InetAddressUtils
	"
%>
<%
    String hostName = InetAddressUtils.getLocalHostName();
%>
<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Send Event" />
  <jsp:param name="headTitle" value="Send Event" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Send Event" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="jquery-ui-base-theme" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="jquery-js" />
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

<form role="form" class="form-horizontal" method="post" name="sendevent" id="form.sendevent" action="admin/postevent.jsp">

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Send Event to OpenNMS</h3>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <label for="input.uei" class="col-sm-2 control-label">Event</label>
          <div class="col-sm-10">
            <select name="uei" class="form-control" id="input.uei" >
              <option value="">--Select One--</option>
              ${model.eventSelect}
            </select>
          </div>
        </div>
        <div class="form-group">
          <label for="uuid" class="col-sm-2 control-label">UUID</label>
          <div class="col-sm-10">
            <input id="uuid" name="uuid" class="form-control" type="text" value="" />
          </div>
        </div>
        <div class="form-group">
          <label for="nodeSelect" class="col-sm-2 control-label">Node ID:</label>
          <div class="col-sm-10">
            <select id="nodeSelect" name="nodeSelect" class="form-control" style="display: none"></select>
          </div>
        </div>
        <div class="form-group">
          <label for="hostname" class="col-sm-2 control-label">Source Hostname:</label>
          <div class="col-sm-10">
            <input id="hostname" name="hostname" class="form-control" type="text" value="<%=hostName%>" />
          </div>
        </div>
        <div class="form-group">
          <label for="interfaceSelect" class="col-sm-2 control-label">Interface:</label>
          <div class="col-sm-10">
            <select id="interfaceSelect" name="interfaceSelect" class="form-control" style="display: none"></select>
          </div>
        </div>
        <div class="form-group">
          <label for="service" class="col-sm-2 control-label">Service:</label>
          <div class="col-sm-10">
            <input id="service" name="service" class="form-control" type="text" value="" />
          </div>
        </div>
        <div class="form-group">
          <label for="service" class="col-sm-2 control-label">Parameters:</label>
          <div class="col-sm-10">
            <div id="parmlist"></div>
            <br/>
            <a id="addparm">Add additional parameter</a>
          </div>
        </div>
        <div class="form-group">
          <label for="description" class="col-sm-2 control-label">Description:</label>
          <div class="col-sm-10">
            <textarea id="description" name="description" class="form-control" rows="5" style="resize: none;"></textarea>
          </div>
        </div>
        <div class="form-group">
          <label for="input_severity" class="col-sm-2 control-label">Severity:</label>
          <div class="col-sm-10">
                <select name="severity" id="input_severity" class="form-control">
                  <option value="">--Select One--</option>
                  <option>Indeterminate</option>
                  <option>Cleared</option>
                  <option>Normal</option>
                  <option>Warning</option>
                  <option>Minor</option>
                  <option>Major</option>
                  <option>Critical</option>
                </select>
          </div>
        </div>
        <div class="form-group">
          <label for="operinstruct" class="col-sm-2 control-label">Operator Instructions:</label>
          <div class="col-sm-10">
            <textarea id="operinstruct" name="operinstruct" class="form-control" rows="5" style="resize: none;"></textarea>
          </div>
        </div>
        <div class="form-group">
          <div class="col-sm-2 col-sm-offset-2">
            <input type="reset" class="btn btn-default"/>
          </div>
        </div>
      </div> <!-- panel-body -->
      <div class="panel-footer">
        <a href="javascript:next()">Send Event &#155;&#155;&#155;</a>
      </div> <!-- panel-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
