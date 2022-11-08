#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#

{{$slackPath := "/opennms/notifd/slack/" -}}

org.opennms.netmgt.notifd.slack.webhookURL={{getv (print $slackPath "webhookURL") "Webhook URL"}}
org.opennms.netmgt.notifd.slack.channel={{getv (print $slackPath "channel") "Webhook"}}
org.opennms.netmgt.notifd.slack.username={{getv (print $slackPath "userName")  "none"}}
org.opennms.netmgt.notifd.slack.iconEmoji={{getv (print $slackPath "iconEmoji")  ""}}
org.opennms.netmgt.notifd.slack.iconURL={{getv (print $slackPath "iconURL") ""}}
org.opennms.netmgt.notifd.slack.useSystemProxy={{getv (print $slackPath "useSystemProxy") "true"}}
