#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
sshPort={{getv "/opennms/karaf/ssh/port" "8101"}}
# OPENNMS: Restrict SSH to localhost only
sshHost={{getv "/opennms/karaf/ssh/host" "0.0.0.0"}}
sshRealm=karaf
hostKey=/opt/opennms/etc/host.key
