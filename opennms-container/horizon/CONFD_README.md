# Configuring Horizon via confd
(instructions for testing/developing confd templates are given at the end of this document)
## Mounting
When starting the Horizon container, mount a yaml file to the following path `/opt/minion/horizon-config.yaml`.

Any configuration provided to confd will overwrite configuration specified as environment variables. Direct overlay of
specific configuration files will overwrite the corresponding config provided by confd.

## Contents
The following describes the keys that can be specified in `horizon-config.yaml` to configure Horizon via confd.

### Slack

```
---
opennms:
  notifd:
    slack:
      channel: alerting
      userName: username
      iconEmoji: :metal:
      iconURL: https://url.com/picture
      useSystemProxy: false
```

Config specified will be written to `etc/opennms.properties.d/_confd.slack.properties`. Check the docs for detailed information about the Slack configuration parameters.
 
---
**NOTE**

When defining the Slack variables within the Docker configuration as environment variables, the environment variables will overwrite the configurations defined in the `horizon-config.yaml`

---
