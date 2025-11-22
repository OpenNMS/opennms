<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

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
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Send Event")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Send Event")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="jquery-ui-base-theme" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="jquery-js" />
</jsp:include>

<script type="text/javascript">
  console.log('${model.eventSelect}', 'loaded from server');
  
  function next() {
    if (document.getElementById("input.uei").selectedIndex == 0) {
      alert("Please select a uei to associate with this event.");
    } else {
      document.getElementById("form.sendevent").submit();
    }
  }

  // Invoke a jQuery function
  $(document).ready(function () {
    // Create the 'combobox' widget which can widgetize a <select> tag
    $.widget("ui.combobox", {
      _create: function () {
        var self = this;
        // Hide the existing tag
        var select = this.element.hide();

        var wrapper = $('<div class="input-group">').appendTo(select.parent());

        // Add an autocomplete text field
        var input = $(
          '<input class="form-control" name="' + self.options.name + '">'
        )
          .appendTo(wrapper)
          .autocomplete({
            source: self.options.jsonUrl,
            delay: 1000,
            change: function (event, ui) {
              if (!ui.item) {
                // remove invalid value, as it didn't match anything
                $(this).val("");
                return false;
              }
              select.val(ui.item.id);
              self._trigger("selected", event, {
                item: select.find("[value='" + ui.item.id + "']"),
              });
            },
            blur: function (event, ui) {
              if (this("widget").is(":visible")) {
                this("close");
                return;
              }
            },
            minLength: 0,
          });

        // Add a dropdown arrow button that will expand the entire list
        $(
          '<div class="input-group-append"><button type="button" class="btn btn-secondary"><i class="fa fa-caret-down"></i></button></div>'
        )
          .attr("tabIndex", -1)
          .attr("title", "Show All Items")
          .insertAfter(input)
          .click(function () {
            // close if already visible
            if (input.autocomplete("widget").is(":visible")) {
              input.autocomplete("close");
              return;
            }
            // pass empty string as value to search for, displaying all results
            input.autocomplete("search", "");
            input.focus();
          });
      },
    });

    $("#nodeSelect").combobox({
      name: "nodeid",
      jsonUrl: "admin/sched-outages/jsonNodes.jsp",
    });
    $("#interfaceSelect").combobox({
      name: "interface",
      jsonUrl: "admin/sched-outages/jsonIpInterfaces.jsp",
    });

    $("#addparm").click(function (event) {
      event.preventDefault();
      var lastNum = 0;
      $("#parmlist > tbody > tr").each(function (index, el) {
        var num = Number($(el).attr("id").substring(4));
        if (lastNum <= num) {
          lastNum = num + 1;
        }
      });

      var nextParm = "parm" + lastNum;
      var tbody = $("#parmlist > tbody");
      var row = $('<tr id="' + nextParm + '"></tr>').appendTo(tbody);
      $(row).append(
        $("<td></td>").append(
          $(
            '<button type="button" role="button" class="btn btn-link"><i class="fa fa-trash"></i></button>'
          )
            .attr("id", nextParm + ".delete")
            .attr("name", nextParm + ".delete")
            .click(function (e) {
              e.preventDefault();
              $("#" + nextParm).remove();
              if ($("#parmlist > tbody > tr").length == 0) {
                $("#parmlist").addClass("invisible");
              }
            })
        )
      );
      $(row).append(
        $("<td></td>").append(
          '<input id="' +
            nextParm +
            '.name" name="' +
            nextParm +
            '.name" type="text" value="" class="form-control" />'
        )
      );
      $(row).append(
        $("<td></td>").append(
          '<input id="' +
            nextParm +
            '.value" name="' +
            nextParm +
            '.value" type="text" value="" class="form-control" />'
        )
      );
      $("#parmlist").removeClass("invisible");
    });
  });

  $(document).ready(function () {
    function initializeUeiAutocomplete() {
      var $select = $("#input\\.uei");
      var $input = $("#ueiAutocomplete");
      var source = $select
        .find("option")
        .map(function () {
          var $option = $(this);
          return {
            label: $option.text().trim(),
            value: $option.attr("value") || $option.text().trim(),
            element: $option,
          };
        })
        .get();

      var autocompleteSource = source.filter(function (item) {
        return item.value !== "" && item.label !== "--Select One--";
      });

      $input.autocomplete({
        minLength: 0,
        delay: 100,
        source: function (request, response) {
          var term = request.term.toLowerCase().trim();

          if (term === "") {
            response(autocompleteSource);
          } else {
            var results = autocompleteSource.filter(function (item) {
              return (
                item.label.toLowerCase().includes(term) ||
                item.value.toLowerCase().includes(term)
              );
            });
            response(results);
          }
        },
        select: function (event, ui) {
          $input.val(ui.item.label);
          $select.val(ui.item.value);
          $select.trigger("change");
          return false;
        },
        focus: function (event, ui) {
          $input.val(ui.item.label);
          return false;
        },
      });
      $("#showAllUei").on("click", function (e) {
        e.preventDefault();
        $input.autocomplete("search", "");
        $input.focus();
      });
      $input.on("blur", function () {
        var currentText = $input.val().trim();
        var matchedItem = autocompleteSource.find(function (item) {
          return item.label === currentText;
        });

        if (matchedItem) {
          $select.val(matchedItem.value);
        } else {
          $input.val("");
          $select.val("");
        }
      });

      var currentSelected = $select.find("option:selected");
      if (currentSelected.length && currentSelected.val() !== "") {
        $input.val(currentSelected.text().trim());
      }
    }

    // Initialize the autocomplete
    initializeUeiAutocomplete();
  });

  function next() {
    var ueiValue = document.getElementById("input.uei").value;
    var ueiInput = document.getElementById("ueiAutocomplete").value;

    if (!ueiValue && ueiInput) {
      alert("Please select a valid event from the list.");
      return;
    }

    if (!ueiValue) {
      alert("Please select a UEI to associate with this event.");
    } else {
      document.getElementById("form.sendevent").submit();
    }
  }
</script>

<form
  role="form"
  class="form"
  method="post"
  name="sendevent"
  id="form.sendevent"
  action="admin/postevent.jsp"
>
  <div class="row">
    <div class="col-md-6">
      <div class="card">
        <div class="card-header">
          <span>Send Event to OpenNMS</span>
        </div>
        <div class="card-body">
          <div class="form-group form-row">
            <label for="input.uei" class="col-sm-3 col-form-label">Event</label>
            <div class="col-sm-9">
              <!-- Hidden select for form submission -->
              <select
                name="uei"
                class="form-control custom-select"
                id="input.uei"
                style="display: none"
              >
                <option value="">--Select One--</option>
                ${model.eventSelect}
              </select>

              <!-- Visible autocomplete input for user interaction -->
              <div class="input-group" id="uei-combobox-wrapper">
                <input
                  id="ueiAutocomplete"
                  name="ueiAutocomplete"
                  class="form-control"
                  placeholder="Type to search events or click dropdown to see all"
                  type="text"
                  autocomplete="off"
                />
                <div class="input-group-append">
                  <button
                    type="button"
                    class="btn btn-secondary"
                    id="showAllUei"
                    title="Show All Events"
                  >
                    <i class="fa fa-caret-down"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="form-group form-row">
            <label for="uuid" class="col-sm-3 col-form-label">UUID</label>
            <div class="col-sm-9">
              <input
                id="uuid"
                name="uuid"
                class="form-control"
                type="text"
                value=""
              />
            </div>
          </div>
          <div class="form-group form-row">
            <label for="nodeSelect" class="col-sm-3 col-form-label"
              >Node ID</label
            >
            <div class="col-sm-9">
              <select
                id="nodeSelect"
                name="nodeSelect"
                class="form-control custom-select"
                style="display: none"
              ></select>
            </div>
          </div>
          <div class="form-group form-row">
            <label for="hostname" class="col-sm-3 col-form-label"
              >Source Hostname</label
            >
            <div class="col-sm-9">
              <input
                id="hostname"
                name="hostname"
                class="form-control"
                type="text"
                value="<%=hostName%>"
              />
            </div>
          </div>
          <div class="form-group form-row">
            <label for="interfaceSelect" class="col-sm-3 col-form-label"
              >Interface</label
            >
            <div class="col-sm-9">
              <select
                id="interfaceSelect"
                name="interfaceSelect"
                class="form-control custom-select"
                style="display: none"
              ></select>
            </div>
          </div>
          <div class="form-group form-row">
            <label for="service" class="col-sm-3 col-form-label">Service</label>
            <div class="col-sm-9">
              <input
                id="service"
                name="service"
                class="form-control"
                type="text"
                value=""
              />
            </div>
          </div>
          <div class="form-group form-row">
            <label for="service" class="col-sm-3 col-form-label"
              >Parameters</label
            >
            <div class="col-sm-9">
              <table
                id="parmlist"
                class="table table-sm table-borderless invisible"
              >
                <thead>
                  <tr>
                    <td>&nbsp;</td>
                    <td>Name</td>
                    <td>Value</td>
                  </tr>
                </thead>
                <tbody></tbody>
              </table>
              <a href id="addparm">Add additional parameter</a>
            </div>
          </div>
          <div class="form-group form-row">
            <label for="description" class="col-sm-3 col-form-label"
              >Description</label
            >
            <div class="col-sm-9">
              <textarea
                id="description"
                name="description"
                class="form-control"
                rows="5"
                style="resize: none"
              ></textarea>
            </div>
          </div>
          <div class="form-group form-row">
            <label for="input_severity" class="col-sm-3 col-form-label"
              >Severity</label
            >
            <div class="col-sm-9">
              <select
                name="severity"
                id="input_severity"
                class="form-control custom-select"
              >
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
          <div class="form-group form-row">
            <label for="operinstruct" class="col-sm-3 col-form-label"
              >Operator Instructions</label
            >
            <div class="col-sm-9">
              <textarea
                id="operinstruct"
                name="operinstruct"
                class="form-control"
                rows="5"
                style="resize: none"
              ></textarea>
            </div>
          </div>
          <div class="form-group form-row">
            <div class="col-sm-3 col-sm-offset-2">
              <input type="reset" class="btn btn-secondary" />
            </div>
          </div>
        </div>
        <!-- card-body -->
        <div class="card-footer">
          <a href="javascript:next()">Send Event &#155;&#155;&#155;</a>
        </div>
        <!-- card-footer -->
      </div>
      <!-- panel -->
    </div>
    <!-- column -->
  </div>
  <!-- row -->
</form>
<style>
  .ui-autocomplete {
    max-height: 40%;
    overflow-y: auto;
    overflow-x: hidden;
    z-index: 1000;
  }
  .ui-autocomplete .ui-menu-item {
    padding: 8px 12px;
    font-size: 14px;
    border-bottom: 1px solid #eee;
  }
  .ui-autocomplete .ui-menu-item:last-child {
    border-bottom: none;
  }
  .ui-autocomplete .ui-state-active {
    background-color: #007bff;
    color: white;
    border: none;
    margin: 0;
  }
</style>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
