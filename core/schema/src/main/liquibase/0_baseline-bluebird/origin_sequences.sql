create sequence public.opennmsnxtid;

alter sequence public.opennmsnxtid owner to opennms;

create sequence public.nodenxtid;

alter sequence public.nodenxtid owner to opennms;

create sequence public.servicenxtid;

alter sequence public.servicenxtid owner to opennms;

create sequence public.eventsnxtid;

alter sequence public.eventsnxtid owner to opennms;

create sequence public.alarmsnxtid;

alter sequence public.alarmsnxtid owner to opennms;

create sequence public.outagenxtid;

alter sequence public.outagenxtid owner to opennms;

create sequence public.notifynxtid;

alter sequence public.notifynxtid owner to opennms;

create sequence public.catnxtid;

alter sequence public.catnxtid owner to opennms;

create sequence public.usernotifnxtid;

alter sequence public.usernotifnxtid owner to opennms;

create sequence public.reportcatalognxtid;

alter sequence public.reportcatalognxtid owner to opennms;

create sequence public.memonxtid;

alter sequence public.memonxtid owner to opennms;

create sequence public.filternextid;

alter sequence public.filternextid owner to opennms;

create sequence public.rulenxtid;

alter sequence public.rulenxtid owner to opennms;

create sequence public.endpointsnxtid;

alter sequence public.endpointsnxtid owner to opennms;

create sequence public.graphnxtid;

alter sequence public.graphnxtid owner to opennms;

create sequence public.bmpcollectornxtid;

alter sequence public.bmpcollectornxtid owner to opennms;

create sequence public.bmprouternxtid;

alter sequence public.bmprouternxtid owner to opennms;

create sequence public.bmppeernxtid;

alter sequence public.bmppeernxtid owner to opennms;

create sequence public.baseattrsnxtid;

alter sequence public.baseattrsnxtid owner to opennms;

create sequence public.bmpunicastnxtid;

alter sequence public.bmpunicastnxtid owner to opennms;

create sequence public.bmpglobalipribsnxtid;

alter sequence public.bmpglobalipribsnxtid owner to opennms;

create sequence public.bmpasninfonxtid;

alter sequence public.bmpasninfonxtid owner to opennms;

create sequence public.bmpasnpathnxtid;

alter sequence public.bmpasnpathnxtid owner to opennms;

create sequence public.bmprouteinfonxtid;

alter sequence public.bmprouteinfonxtid owner to opennms;

create sequence public.bmpipriblognxtid;

alter sequence public.bmpipriblognxtid owner to opennms;

create sequence public.bmpstatsbypeernxtid;

alter sequence public.bmpstatsbypeernxtid owner to opennms;

create sequence public.bmpstatsbyasnnxtid;

alter sequence public.bmpstatsbyasnnxtid owner to opennms;

create sequence public.bmpstatsbyprefixnxtid;

alter sequence public.bmpstatsbyprefixnxtid owner to opennms;

create sequence public.bmpstatspeerribnxtid;

alter sequence public.bmpstatspeerribnxtid owner to opennms;

create sequence public.bmpstatsiporiginsnxtid;

alter sequence public.bmpstatsiporiginsnxtid owner to opennms;

create sequence public.bmprpkiinfonxtid;

alter sequence public.bmprpkiinfonxtid owner to opennms;

create sequence public.deviceconfignxtid;

alter sequence public.deviceconfignxtid owner to opennms;


-- TODO MVR this is new but it seems that it is not used. Verify...
create sequence public.usage_analytics_id_seq start with 2;
alter sequence public.usage_analytics_id_seq owned by public.usage_analytics.id;
alter sequence public.usage_analytics_id_seq owner to opennms;