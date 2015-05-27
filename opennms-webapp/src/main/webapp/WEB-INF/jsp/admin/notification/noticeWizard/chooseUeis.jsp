<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
	import="org.opennms.web.admin.notification.noticeWizard.*"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Choose Event" />
  <jsp:param name="headTitle" value="Choose Event" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/noticeWizard/eventNotices.htm'>Event Notifications</a>" />
  <jsp:param name="breadcrumb" value="Choose Event" />
</jsp:include>

<script type="text/javascript" >

    function next()
    {
        if (document.events.uei.selectedIndex==-1)
        {
            alert("Please select a uei to associate with this notification.");
        }
        else
        {
            document.events.submit();
        }
    }

$(document).ready(function() {

  function toggleRegexpField() {
    var ueiLabel = $("#uei option:selected").text();
    $('#regexp').prop('disabled', ueiLabel !== 'REGEX_FIELD');
  }

  var allUeiOptions = $("#uei > option");
  var unavailableUeiOptGroup = $("#uei > optgroup");

  function filterUeiList() {
    var filterText = $("#uei-list-filter").val().toLowerCase();
    $("#uei").empty();
    if (filterText.length == 0) {
      $("#uei").append(allUeiOptions);
      $("#uei").append(unavailableUeiOptGroup);
    } else {
      allUeiOptions.each(function ( index, element ) {
        if ( $(this).text().toLowerCase().indexOf(filterText) != -1) {
          $("#uei").append( $(this) );
        }
      });
    }
    $('#filteringModal').modal('hide');
    $('#uei-list-filter').focus();
  }

  $("#uei-list-filter").keydown(function(event) {
    if (event.which == 27) {
      event.preventDefault();
      if ($("#uei-list-filter").val().length > 0) {
        $('#uei-list-filter').val("");
        $('#filteringModal').modal('show');
      }
    }
  });

  $("#uei").keydown(function(event) {
    if (event.which == 27) {
      event.preventDefault();
      if ($("#uei-list-filter").val().length > 0) {
        $('#uei-list-filter').val("");
        $('#filteringModal').modal('show');
      }
    }
  });

  $("#uei-list-filter").keypress(function(event) {
    if (event.which == 13) {
      event.preventDefault();
      $('#filteringModal').modal('show');
    }
  });

  $("#filteringModal").on( "shown.bs.modal", function() { filterUeiList(); } );

  $("select#uei").change(function(e) {
    toggleRegexpField();
  });

  $('#regexp').change(function(e) {
    var value = e.target.value;
    if (!value.match("^~")) {
      alert("Invalid regex field: [" + value + "], value does not start with ~\n" +
        "Resetting value back to default.\n"
      );
      e.target.value = e.target.defaultValue;
      return false;
    }
    var uei = $('#uei')[0];
    $(uei.options[uei.selectedIndex]).val(value);
    uei.selectmenu('refresh');
  });

  toggleRegexpField();

  if ($('#regexp').val() === '~^$') {
    $('#regexp').prop('disabled', true);
  }

});

</script>

<div id="filteringModal" class="modal fade" tabindex="-1">
  <div class="modal-dialog modal-sm">
    <div class="modal-content">
      <div class="modal-body">
        <i class="fa fa-spinner fa-spin"></i> Filtering
      </div>
    </div>
  </div>
</div>

<h2>${model.title}</h2>

<form method="post" name="events"
      action="admin/notification/noticeWizard/notificationWizard" >
<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_UEIS%>"/>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Choose the event UEI that will trigger this notification.</h3>
      </div>
      <table class="table table-condensed">
        <tr>
          <td valign="top" align="left">
            <div class="form-group">
              <label for="uei" class="control-label">Events</label>
              <input id="uei-list-filter" name="uei-list-filter" type="text" class="form-control" size="96" value="" placeholder="Filter displayed events..." />
              <select id="uei" name="uei" class="form-control" size="20" >
              ${model.eventSelect}
              </select>
            </div>
            <div class="form-group">
              <label for="regexp" class="control-label">Regular Expression Field</label>
              <input id="regexp" name="regexp" type="text" class="form-control" size="96" value="${model.noticeUei}" />
            </div>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="reset" class="btn btn-default"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a HREF="javascript:next()">Next &#155;&#155;&#155;</a>
          </td>
        </tr>
      </table>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
