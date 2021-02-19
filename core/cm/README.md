# Configuration Management API (CM-API)

The CM-API is used to help manage the lifecycle of configuration in OpenNMS.
It also provides rich APIs to help extend and automate the platform.

## Addressing

Base path: `/api/v2/cm/vacuumd`

### VacuumdConfiguration
```
GET / => complete VacuumdConfiguration object
GET /?jsonpath=$.store.* => filter the resulting JSON

POST / => complete VacuumdConfiguration object -> reject if existing?
PUT / => complete VacuumdConfiguration object -> reject if none existing?

PATCH / => patch for VacuumdConfiguration object

DELETE / => remove VacuumdConfiguration object
```

### statements
```
GET /statement => retrieve array of statements

POST /statement => insert new statement at the end of the array
POST /statement?index=0,-1,2 => insert a new statement at the given position

GET /statement/0/ => retrieve the first statement
PUT /statement/0/ => replace the first statement

DELETE /statement/0/ => delete the first statement
PATCH /statement/0/ => patch the first statement
```

### automations

```
GET /automations => retrieve automations object

POST /automations => insert new automations object -> reject if existing?
PUT /automations => update complete automations object -> reject if none existing?
```

```json
{
  "automation": [{
    "action-name": "test"
  }]
}
```

### automation

```
GET /automations/automation => retrieve array object

... array handling like statements ...
```

```json
[{
  "action-name": "test"
}]
```


### building the via shell


```
<VacuumdConfiguration period="86400000" >
    <statement>
        <!-- this deletes all the snmpInterfaces that have been marked as deleted -->
        DELETE FROM snmpInterface WHERE snmpInterface.snmpCollect = 'D';
    </statement>
    
    <automations>
        <automation name="clearPathOutages" interval="30000" active="true"
                    trigger-name="selectPathOutagesNodes"
                    action-name="clearPathOutages" />
    </automations>
    
    <actions>
        <action name="clearPathOutages" >
            <statement>
                UPDATE alarms
                SET severity = 2, lastautomationtime = ${_ts}
                WHERE eventuei = 'uei.opennms.org/nodes/pathOutage'
                AND nodeid = ${_nodeid}
                AND lasteventtime &lt;= ${_eventtime_from_node_up}
            </statement>
        </action>
    </actions>
</VacuumdConfiguration>
```

```
cm:set /vacuumd/period 24h
cm:set /vacuumd/statement/0/sql "DELETE FROM snmpInterface WHERE snmpInterface.snmpCollect = 'D';"
cm:set /vacuumd/statement/0/comment "this deletes all the snmpInterfaces that have been marked as deleted"

cm:set /vacuumd/automations/automation/clearPathOutages/interval 30000
cm:set /vacuumd/automations/automation/clearPathOutages/active true
cm:set /vacuumd/automations/automation/clearPathOutages/trigger-name selectPathOutagesNodes
cm:set /vacuumd/automations/automation/clearPathOutages/action-name clearPathOutages

cm:set /vacuumd/triggers/trigger/selectPathOutagesNodes/operator "&gt;="
cm:set /vacuumd/triggers/trigger/selectPathOutagesNodes/row-count 1
cm:set /vacuumd/triggers/trigger/selectPathOutagesNodes/statement "SELECT nodeId AS _nodeid,
                now() AS _ts,
                lastEventTime AS _eventtime_from_node_up
                FROM alarms
                WHERE eventuei='uei.opennms.org/nodes/nodeUp'"

cm:set /vacuumd/actions/action/clearPathOutages/statement "UPDATE alarms
                SET severity = 2, lastautomationtime = ${_ts}
                WHERE eventuei = 'uei.opennms.org/nodes/pathOutage'
                AND nodeid = ${_nodeid}
                AND lasteventtime &lt;= ${_eventtime_from_node_up}"
```
