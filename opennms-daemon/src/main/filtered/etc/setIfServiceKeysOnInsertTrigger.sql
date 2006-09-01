--First step to refactoring ipInterface, snmpInterface tables to have actual IDs
--This trigger puts ifService IDs in the outage table

DROP TRIGGER setIfServiceKeysOnInsertTrigger ON outages;

CREATE TRIGGER setIfServiceKeysOnInsertTrigger
   BEFORE INSERT
   ON outages FOR EACH ROW
   EXECUTE PROCEDURE setIfServiceKeysOnInsert();
