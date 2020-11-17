#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#

{{ if getenv "OPENNMS_NOTIFD_SLACK_CHANNEL" }}

org.opennms.netmgt.notifd.slack.channel={{getenv "OPENNMS_NOTIFD_SLACK_CHANNEL" "Webhook"}}
org.opennms.netmgt.notifd.slack.username={{getenv "OPENNMS_NOTIFD_SLACK_USERNAME" "none"}}
org.opennms.netmgt.notifd.slack.iconEmoji={{getenv "OPENNMS_NOTIFD_SLACK_ICONEMOJI" ""}}
org.opennms.netmgt.notifd.slack.iconURL={{getenv "OPENNMS_NOTIFD_SLACK_ICONURL" ""}}
org.opennms.netmgt.notifd.slack.useSystemProxy={{getenv "OPENNMS_NOTIFD_SLACK_USESYSTEMPROXY" "true"}}

{{ else }}

{{$slackPath := "/opennms/notifd/slack/" -}}

org.opennms.netmgt.notifd.slack.channel={{getv (print $slackPath "channel") "Webhook"}}
org.opennms.netmgt.notifd.slack.username={{getv (print $slackPath "userName")  "none"}}
org.opennms.netmgt.notifd.slack.iconEmoji={{getv (print $slackPath "iconEmoji")  ""}}
org.opennms.netmgt.notifd.slack.iconURL={{getv (print $slackPath "iconURL") ""}}
org.opennms.netmgt.notifd.slack.useSystemProxy={{getv (print $slackPath "useSystemProxy") "true"}}

{{ end }}
