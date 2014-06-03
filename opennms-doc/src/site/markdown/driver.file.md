### File driver
This driver offers the ability to call the integration application and get OpenNMS requisition XML files as an result. This driver requires the following parameters in the `global.properties`:

* `driver` = file (selects the file driver)
* `target` = /tmp/requisitions (the folder to store the requisition file)
* `requisitions` = * (a filter for the requisitions to generate)
