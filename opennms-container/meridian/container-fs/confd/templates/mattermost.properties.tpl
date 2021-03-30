#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#

{{$mattermostPath := "/opennms/notifd/mattermost/" -}}

org.opennms.netmgt.notifd.mattermost.webhookURL={{getv (print $mattermostPath "webhookURL") "Webhook URL"}}
org.opennms.netmgt.notifd.mattermost.channel={{getv (print $mattermostPath "channel") "Webhook"}}
org.opennms.netmgt.notifd.mattermost.username={{getv (print $mattermostPath "userName")  "none"}}
org.opennms.netmgt.notifd.mattermost.iconEmoji={{getv (print $mattermostPath "iconEmoji")  ""}}
org.opennms.netmgt.notifd.mattermost.iconURL={{getv (print $mattermostPath "iconURL") ""}}
org.opennms.netmgt.notifd.mattermost.useSystemProxy={{getv (print $mattermostPath "useSystemProxy") "true"}}
