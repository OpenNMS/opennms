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
