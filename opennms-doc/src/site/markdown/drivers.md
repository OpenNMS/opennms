## Driver
The driver is responsible for the overall modus operandi.
It defines how to request a requisition and how the tool is providing the requeistion.
The configuration for the drivers is located in the `global.properties` file. 
Configuration of the loglevel is also located in the `global.properties` file. The parameters are listed below.

    loglevel = INFO

| Level | Default|
|-------|-------:|
| ALL   |        |
| TRACE |        |
| DEBUG |        |
| INFO  |   *    |
| WARN  |        |
| ERROR |        |
| OFF   |        |

At the moment a http driver and a file driver are provided.