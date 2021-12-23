Feature: OpenNMS Alarm Daemon Rest

  Scenario: Admin user request /alarms/list endpoint with JSON
    Given application base url in system property "application.base-url"
    Given http username "admin" password "admin"
    Given JSON accept encoding
    Then send GET request at path "/cxf/alarms/list" with retry timeout 20000
    Then verify the response code 202 was returned
    Then DEBUG dump the response body
    Then parse the JSON response
    Then verify JSON path expressions match
      |  totalCount == 0 |
