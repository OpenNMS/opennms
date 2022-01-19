Feature: OpenNMS Alarm Daemon Rest

  Scenario: Add SENTINEL monitoring system to the database
    Given DB url in system property "database.url"
    Given DB username "postgres" and password "ignored"
    Then execute SQL statement "insert into monitoringsystems (id,location,type) VALUES ('00000000-0000-0000-0000-000000ddba11', 'SENTINEL', 'System')"

  Scenario: Ensure Events endpoints are reachable
    Given application base url in system property "application.base-url"
    Given http username "admin" password "admin"
    Then send GET request at path "/cxf/events/count" with retry timeout 20000

  Scenario: Admin user request /alarms/list endpoint with JSON
    Given application base url in system property "application.base-url"
    Given http username "admin" password "admin"
    Given JSON accept encoding
    Then send GET request at path "/cxf/alarms/list" with retry timeout 20000
    Then verify the response code 202 was returned
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      | totalCount == 0 |

  Scenario: Admin user request /alarms/list endpoint with JSON
    Given application base url in system property "application.base-url"
    Given http username "admin" password "admin"
    Given JSON accept encoding
    Given XML content type
    Given POST request body in resource "test-data/event001.xml"
    Then send POST request at path "/cxf/events"
    Then verify the response code 202 was returned
    Then send GET request at path "/cxf/alarms/list" with retry timeout 20000
    Then verify the response code 202 was returned
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      | totalCount == 1                                                       |
      | alarm[0].uei == uei.opennms.org/alarms/trigger                        |
      | alarm[0].logMessage == A problem has been triggered on //x-service-x. |
      | alarm[0].lastEvent.source == x-source-x                               |
