DELETE FROM ifServices WHERE nodeID = 6;
DELETE FROM ipInterface WHERE nodeID = 6;
DELETE FROM node WHERE nodeID = 6;

INSERT INTO node (nodeID, dpName, nodeCreateTime, nodeType) VALUES (6, 'localhost', '30-Aug-2001 12:45:00', 'A');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (6, '209.116.71.164', '209.116.71.164', 'A');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (6, '209.116.71.164', 1, 'A', 'P', 'N');

