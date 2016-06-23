OpenNMS-UI-Requisitions
=======================

A user interface to manage OpenNMS Requisition implemented with AngularJS and Bootstrap 3

This project was designed and implemented to work with OpenNMS 17 or newer.

The current implementation of the application retrieves all the configured requisitions from the OpenNMS server (deployed or not) using the ReST API, and stores the merged data on an internal cache on the browser to improve the user experience and the response time of the application. For this reason, this application is not intended to be used by several users at the same time.

Every time you perform a change on any of the requisitions component and a save operation is requested, the application pushes the change to the OpenNMS server using the ReST API and updates the internal cache.

For a big number of requisitions and nodes, it is required to wait several minutes until all the requisitions are retrieved from the server. This is a delay inherent to the OpenNMS server. The relevant fact is that this delay happens only once, as the rest of read operations are going to be performed against the internal cache, unless the user instructs the application to re-read the data from the server.

Despite the initial delay, the overall performance of the application is drastically faster compared with the current implementation of the WebUI for managing requisitions in OpenNMS.

Compilation Instructions
=======================

Follow this step-by-step guide for installing and using the UI with OpenNMS:

* Install NodeJS (http://www.nodejs.org/)

* Install Required Libraries through NodeJS:

```
sudo npm install -g grunt bower 
```

* Install Third-Party libraries on the project directory

```
npm install
bower install
```
