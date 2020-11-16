#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#

export OPENNMS_NOTIFD_SLACK_CHANNEL
export OPENNMS_NOTIFD_SLACK_USERNAME
export OPENNMS_NOTIFD_SLACK_ICONEMOJI
export OPENNMS_NOTIFD_SLACK_ICONURL
export OPENNMS_NOTIFD_SLACK_USESYSTEMPROXY

{{$slackPath := "/opennms/notifd/slack/" -}}

# Configure storage strategy
org.opennms.netmgt.notifd.slack.channel={{(getenv (print $slackPath "OPENNMS_NOTIFD_SLACK_CHANNEL") "Webhook"}}
org.opennms.netmgt.notifd.slack.username={{getenv (print $slackPath "OPENNMS_NOTIFD_SLACK_USERNAME") "none"}}
org.opennms.netmgt.notifd.slack.iconEmoji={{getenv (print $slackPath "OPENNMS_NOTIFD_SLACK_ICONEMOJI") ""}}
org.opennms.netmgt.notifd.slack.iconURL={{getenv (print $slackPath "OPENNMS_NOTIFD_SLACK_ICONURL") ""}}
org.opennms.netmgt.notifd.slack.useSystemProxy={{getenv (print $slackPath "OPENNMS_NOTIFD_SLACK_USESYSTEMPROXY") "true"}}
