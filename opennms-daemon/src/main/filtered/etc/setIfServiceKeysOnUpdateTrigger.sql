--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts ifService IDs in the outage table

DROP TRIGGER setIfServiceKeysOnUpdateTrigger ON outages;

CREATE TRIGGER setIfServiceKeysOnUpdateTrigger
   BEFORE UPDATE
   ON outages FOR EACH ROW
   EXECUTE PROCEDURE setIfServiceKeysOnUpdate();
