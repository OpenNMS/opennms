DELETE FROM ifServices WHERE nodeID = 5;
DELETE FROM ipInterface WHERE nodeID = 5;
DELETE FROM node WHERE nodeID = 5;

INSERT INTO node (nodeID, dpName, nodeCreateTime, nodeType) VALUES (5, 'localhost', '30-Aug-2001 12:45:00', 'A');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (5, '192.168.0.195', '192.168.0.195', 'A');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (5, '192.168.0.195', 1, 'A', 'P', 'N');

