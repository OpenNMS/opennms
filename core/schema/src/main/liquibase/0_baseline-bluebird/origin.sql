create database opennms
    with owner postgres;

grant connect, create, temporary on database opennms to opennms;

create table public.databasechangeloglock
(
    id          integer not null
        primary key,
    locked      boolean not null,
    lockgranted timestamp,
    lockedby    varchar(255)
);

alter table public.databasechangeloglock
    owner to opennms;

create table public.databasechangelog
(
    id            varchar(255) not null,
    author        varchar(255) not null,
    filename      varchar(255) not null,
    dateexecuted  timestamp    not null,
    orderexecuted integer      not null,
    exectype      varchar(10)  not null,
    md5sum        varchar(35),
    description   varchar(255),
    comments      varchar(255),
    tag           varchar(255),
    liquibase     varchar(20),
    contexts      varchar(255),
    labels        varchar(255),
    deployment_id varchar(10)
);

alter table public.databasechangelog
    owner to opennms;

create table public.servicemap
(
    ipaddr         text         not null,
    servicemapname varchar(255) not null
);

alter table public.servicemap
    owner to opennms;

create index servicemap_name_idx
    on public.servicemap (servicemapname);

create index servicemap_ipaddr_idx
    on public.servicemap (ipaddr);

create table public.service
(
    serviceid   integer default nextval('servicenxtid'::regclass) not null
        constraint pk_serviceid
            primary key,
    servicename varchar(255)                                      not null
        unique
);

alter table public.service
    owner to opennms;

create table public.events
(
    eventid                 integer                  not null
        constraint pk_eventid
            primary key,
    eventuei                varchar(256)             not null,
    nodeid                  integer,
    eventtime               timestamp with time zone not null,
    eventhost               varchar(256),
    eventsource             varchar(128)             not null,
    ipaddr                  text,
    eventsnmphost           varchar(256),
    serviceid               integer,
    eventsnmp               varchar(256),
    eventcreatetime         timestamp with time zone not null,
    eventdescr              text,
    eventloggroup           varchar(32),
    eventlogmsg             text,
    eventseverity           integer                  not null,
    eventpathoutage         varchar(1024),
    eventcorrelation        varchar(1024),
    eventsuppressedcount    integer,
    eventoperinstruct       text,
    eventautoaction         varchar(256),
    eventoperaction         varchar(256),
    eventoperactionmenutext varchar(64),
    eventnotification       varchar(128),
    eventtticket            varchar(128),
    eventtticketstate       integer,
    eventforward            varchar(256),
    eventmouseovertext      varchar(64),
    eventlog                char                     not null,
    eventdisplay            char                     not null,
    eventackuser            varchar(256),
    eventacktime            timestamp with time zone,
    alarmid                 integer,
    ifindex                 integer,
    systemid                text                     not null
);

alter table public.events
    owner to opennms;

create index events_uei_idx
    on public.events (eventuei);

create index events_nodeid_idx
    on public.events (nodeid);

create index events_serviceid_idx
    on public.events (serviceid);

create index events_time_idx
    on public.events (eventtime);

create index events_severity_idx
    on public.events (eventseverity);

create index events_log_idx
    on public.events (eventlog);

create index events_display_idx
    on public.events (eventdisplay);

create index events_ackuser_idx
    on public.events (eventackuser);

create index events_acktime_idx
    on public.events (eventacktime);

create index events_alarmid_idx
    on public.events (alarmid);

create index events_nodeid_display_ackuser
    on public.events (nodeid, eventdisplay, eventackuser);

create index events_ipaddr_idx
    on public.events (ipaddr);

create table public.categories
(
    categoryid          integer default nextval('catnxtid'::regclass) not null
        constraint category_pkey
            primary key,
    categoryname        text                                          not null,
    categorydescription varchar(256)
);

alter table public.categories
    owner to opennms;

create unique index category_idx
    on public.categories (categoryname);

create table public.applications
(
    id   integer     not null
        primary key,
    name varchar(32) not null
);

alter table public.applications
    owner to opennms;

create unique index applications_name_idx
    on public.applications (name);

create table public.statisticsreport
(
    id               integer default nextval('opennmsnxtid'::regclass) not null
        constraint pk_statisticsreport_id
            primary key,
    startdate        timestamp with time zone                          not null,
    enddate          timestamp with time zone                          not null,
    name             varchar(63)                                       not null,
    description      varchar(256)                                      not null,
    jobstarteddate   timestamp with time zone                          not null,
    jobcompleteddate timestamp with time zone                          not null,
    purgedate        timestamp with time zone                          not null
);

alter table public.statisticsreport
    owner to opennms;

create index statisticsreport_startdate
    on public.statisticsreport (startdate);

create index statisticsreport_name
    on public.statisticsreport (name);

create index statisticsreport_purgedate
    on public.statisticsreport (purgedate);

create table public.resourcereference
(
    id         integer default nextval('opennmsnxtid'::regclass) not null
        constraint pk_resourcereference_id
            primary key,
    resourceid varchar(255)                                      not null
);

alter table public.resourcereference
    owner to opennms;

create unique index resourcereference_resourceid
    on public.resourcereference (resourceid);

create table public.statisticsreportdata
(
    id         integer default nextval('opennmsnxtid'::regclass) not null
        constraint pk_statsdata_id
            primary key,
    reportid   integer                                           not null
        constraint fk_statsdata_reportid
            references public.statisticsreport
            on delete cascade,
    resourceid integer                                           not null
        constraint fk_statsdata_resourceid
            references public.resourcereference
            on delete cascade,
    value      double precision                                  not null
);

alter table public.statisticsreportdata
    owner to opennms;

create unique index statsdata_unique
    on public.statisticsreportdata (reportid, resourceid);

create table public.qrtz_job_details
(
    job_name          varchar(200) not null,
    job_group         varchar(200) not null,
    description       varchar(250),
    job_class_name    varchar(250) not null,
    is_durable        boolean      not null,
    requests_recovery boolean      not null,
    job_data          bytea,
    is_nonconcurrent  boolean      not null,
    is_update_data    boolean      not null,
    sched_name        varchar(120) not null,
    constraint pk_qrtz_job_details
        primary key (sched_name, job_name, job_group)
);

alter table public.qrtz_job_details
    owner to opennms;

create index idx_qrtz_j_req_recovery
    on public.qrtz_job_details (sched_name, requests_recovery);

create index idx_qrtz_j_grp
    on public.qrtz_job_details (sched_name, job_group);

create table public.qrtz_triggers
(
    trigger_name   varchar(200) not null,
    trigger_group  varchar(200) not null,
    job_name       varchar(200) not null,
    job_group      varchar(200) not null,
    description    varchar(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    trigger_state  varchar(16)  not null,
    trigger_type   varchar(8)   not null,
    start_time     bigint       not null,
    end_time       bigint,
    calendar_name  varchar(250),
    misfire_instr  smallint,
    job_data       bytea,
    priority       integer,
    sched_name     varchar(120) not null,
    constraint pk_qrtz_triggers
        primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_triggers_job_details
        foreign key (sched_name, job_name, job_group) references public.qrtz_job_details
);

alter table public.qrtz_triggers
    owner to opennms;

create index idx_qrtz_t_state
    on public.qrtz_triggers (sched_name, trigger_state);

create index idx_qrtz_t_next_fire_time
    on public.qrtz_triggers (sched_name, next_fire_time);

create index idx_qrtz_t_nft_st
    on public.qrtz_triggers (sched_name, trigger_state, next_fire_time);

create index idx_qrtz_t_nft_misfire
    on public.qrtz_triggers (sched_name, misfire_instr, next_fire_time);

create index idx_qrtz_t_nft_st_misfire
    on public.qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);

create index idx_qrtz_t_g
    on public.qrtz_triggers (sched_name, trigger_group);

create index idx_qrtz_t_n_g_state
    on public.qrtz_triggers (sched_name, trigger_group, trigger_state);

create index idx_qrtz_t_nft_st_misfire_grp
    on public.qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);

create index idx_qrtz_t_n_state
    on public.qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);

create index idx_qrtz_t_jg
    on public.qrtz_triggers (sched_name, job_group);

create index idx_qrtz_t_j
    on public.qrtz_triggers (sched_name, job_name, job_group);

create index idx_qrtz_t_c
    on public.qrtz_triggers (sched_name, calendar_name);

create table public.qrtz_simple_triggers
(
    trigger_name    varchar(200) not null,
    trigger_group   varchar(200) not null,
    repeat_count    bigint       not null,
    repeat_interval bigint       not null,
    times_triggered bigint       not null,
    sched_name      varchar(120) not null,
    constraint pk_qrtz_simple_triggers
        primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_simple_triggers_triggers
        foreign key (sched_name, trigger_name, trigger_group) references public.qrtz_triggers
);

alter table public.qrtz_simple_triggers
    owner to opennms;

create table public.qrtz_cron_triggers
(
    trigger_name    varchar(200) not null,
    trigger_group   varchar(200) not null,
    cron_expression varchar(120) not null,
    time_zone_id    varchar(80),
    sched_name      varchar(120) not null,
    constraint pk_qrtz_cron_triggers
        primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_cron_triggers_triggers
        foreign key (sched_name, trigger_name, trigger_group) references public.qrtz_triggers
);

alter table public.qrtz_cron_triggers
    owner to opennms;

create table public.qrtz_blob_triggers
(
    trigger_name  varchar(200) not null,
    trigger_group varchar(200) not null,
    blob_data     bytea,
    sched_name    varchar(120) not null,
    constraint pk_qrtz_blob_triggers
        primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_blob_triggers_triggers
        foreign key (sched_name, trigger_name, trigger_group) references public.qrtz_triggers
);

alter table public.qrtz_blob_triggers
    owner to opennms;

create table public.qrtz_calendars
(
    calendar_name varchar(200) not null,
    calendar      bytea        not null,
    sched_name    varchar(120) not null,
    constraint pk_qrtz_calendars
        primary key (sched_name, calendar_name)
);

alter table public.qrtz_calendars
    owner to opennms;

create table public.qrtz_paused_trigger_grps
(
    trigger_group varchar(200) not null,
    sched_name    varchar(120) not null,
    constraint pk_qrtz_paused_trigger_grps
        primary key (sched_name, trigger_group)
);

alter table public.qrtz_paused_trigger_grps
    owner to opennms;

create table public.qrtz_fired_triggers
(
    entry_id          varchar(95)  not null,
    trigger_name      varchar(200) not null,
    trigger_group     varchar(200) not null,
    instance_name     varchar(200) not null,
    fired_time        bigint       not null,
    state             varchar(16)  not null,
    job_name          varchar(200),
    job_group         varchar(200),
    requests_recovery boolean,
    priority          integer      not null,
    sched_time        bigint       not null,
    is_nonconcurrent  boolean,
    sched_name        varchar(120) not null,
    constraint pk_qrtz_fired_triggers
        primary key (sched_name, entry_id)
);

alter table public.qrtz_fired_triggers
    owner to opennms;

create index idx_qrtz_ft_tg
    on public.qrtz_fired_triggers (sched_name, trigger_group);

create index idx_qrtz_ft_t_g
    on public.qrtz_fired_triggers (sched_name, trigger_name, trigger_group);

create index idx_qrtz_ft_trig_inst_name
    on public.qrtz_fired_triggers (sched_name, instance_name);

create index idx_qrtz_ft_inst_job_req_rcvry
    on public.qrtz_fired_triggers (sched_name, instance_name, requests_recovery);

create index idx_qrtz_ft_jg
    on public.qrtz_fired_triggers (sched_name, job_group);

create index idx_qrtz_ft_j_g
    on public.qrtz_fired_triggers (sched_name, job_name, job_group);

create table public.qrtz_scheduler_state
(
    instance_name     varchar(200) not null,
    last_checkin_time bigint       not null,
    checkin_interval  bigint       not null,
    sched_name        varchar(120) not null,
    constraint pk_qrtz_scheduler_state
        primary key (sched_name, instance_name)
);

alter table public.qrtz_scheduler_state
    owner to opennms;

create table public.qrtz_locks
(
    lock_name  varchar(40)  not null,
    sched_name varchar(120) not null,
    constraint pk_qrtz_locks
        primary key (sched_name, lock_name)
);

alter table public.qrtz_locks
    owner to opennms;

create table public.acks
(
    id        integer                  default nextval('opennmsnxtid'::regclass) not null
        primary key,
    acktime   timestamp with time zone default now()                             not null,
    ackuser   varchar(64)              default 'admin'::character varying        not null,
    acktype   integer                  default 1                                 not null,
    ackaction integer                  default 1                                 not null,
    log       varchar(128),
    refid     integer
);

alter table public.acks
    owner to opennms;

create index ack_time_idx
    on public.acks (acktime);

create index ack_user_idx
    on public.acks (ackuser);

create index ack_refid_idx
    on public.acks (refid);

create table public.category_group
(
    categoryid integer     not null
        constraint categoryid_fkey2
            references public.categories
            on delete cascade,
    groupid    varchar(64) not null
);

alter table public.category_group
    owner to opennms;

create index catid_idx3
    on public.category_group (categoryid);

create index catgroup_idx
    on public.category_group (groupid);

create unique index catgroup_unique_idx
    on public.category_group (categoryid, groupid);

create table public.reportcatalog
(
    id       integer                  not null,
    reportid varchar(256)             not null,
    title    varchar(256)             not null,
    date     timestamp with time zone not null,
    location varchar(256)             not null
);

alter table public.reportcatalog
    owner to opennms;

create table public.accesslocks
(
    lockname varchar(40) not null
        constraint "pk_accessLocks"
            primary key
);

alter table public.accesslocks
    owner to opennms;

create table public.memos
(
    id           integer not null
        primary key,
    created      timestamp with time zone,
    updated      timestamp with time zone,
    author       varchar(256),
    body         text,
    reductionkey varchar(256),
    type         varchar(64),
    unique (reductionkey, type)
);

alter table public.memos
    owner to opennms;

create table public.accesspoints
(
    physaddr         varchar(32)  not null
        constraint pk_physaddr
            primary key,
    nodeid           integer,
    pollingpackage   varchar(256) not null,
    status           integer,
    controlleripaddr varchar(40)
);

alter table public.accesspoints
    owner to opennms;

create index accesspoint_package_idx
    on public.accesspoints (pollingpackage);

create table public.filterfavorites
(
    filterid   integer     not null
        constraint pk_filterid
            primary key,
    username   varchar(50) not null,
    filtername text        not null,
    page       varchar(25) not null,
    filter     text        not null
);

alter table public.filterfavorites
    owner to opennms;

create unique index filternamesidx
    on public.filterfavorites (username, filtername, page);

create table public.hwentityattributetype
(
    id          integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    attribname  varchar(128)                                      not null,
    attriboid   varchar(128)                                      not null,
    attribclass varchar(32)                                       not null
);

alter table public.hwentityattributetype
    owner to opennms;

create unique index hwentityattributetype_unique_name_dx
    on public.hwentityattributetype (attribname);

create unique index hwentityattributetype_unique_oid_idx
    on public.hwentityattributetype (attriboid);

create table public.monitoringsystems
(
    id           text not null
        primary key,
    label        text,
    location     text not null,
    type         text not null,
    status       text,
    last_updated timestamp with time zone,
    version      text
);

alter table public.monitoringsystems
    owner to opennms;

create table public.monitoringsystemsproperties
(
    monitoringsystemid text not null
        constraint monitoringsystemsproperties_fkey
            references public.monitoringsystems
            on delete cascade,
    property           text not null,
    propertyvalue      text
);

alter table public.monitoringsystemsproperties
    owner to opennms;

create index monitoringsystemsproperties_id_idx
    on public.monitoringsystemsproperties (monitoringsystemid);

create unique index monitoringsystemsproperties_id_property_idx
    on public.monitoringsystemsproperties (monitoringsystemid, property);

create table public.monitoringlocations
(
    id             text not null
        primary key,
    monitoringarea text not null,
    geolocation    text,
    latitude       double precision,
    longitude      double precision,
    priority       integer
);

alter table public.monitoringlocations
    owner to opennms;

create table public.node
(
    nodeid             integer                  not null
        constraint pk_nodeid
            primary key,
    nodecreatetime     timestamp with time zone not null,
    nodeparentid       integer,
    nodetype           char,
    nodesysoid         varchar(256),
    nodesysname        varchar(256),
    nodesysdescription varchar(256),
    nodesyslocation    varchar(256),
    nodesyscontact     varchar(256),
    nodelabel          varchar(256)             not null,
    nodelabelsource    char,
    nodenetbiosname    varchar(16),
    nodedomainname     varchar(16),
    operatingsystem    varchar(64),
    lastcapsdpoll      timestamp with time zone,
    foreignsource      varchar(64),
    foreignid          varchar(64),
    location           text                     not null
        constraint fk_node_location
            references public.monitoringlocations
            on update cascade,
    last_ingress_flow  timestamp with time zone,
    last_egress_flow   timestamp with time zone
);

alter table public.node
    owner to opennms;

create index node_id_type_idx
    on public.node (nodeid, nodetype);

create index node_label_idx
    on public.node (nodelabel);

create unique index node_foreign_unique_idx
    on public.node (foreignsource, foreignid);

create index node_sysoid_idx
    on public.node (nodesysoid);

create index node_sysdescr_idx
    on public.node (nodesysdescription);

create table public.snmpinterface
(
    id                integer    default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid            integer                                              not null
        constraint fk_nodeid2
            references public.node
            on delete cascade,
    snmpphysaddr      varchar(64),
    snmpifindex       integer                                              not null,
    snmpifdescr       text,
    snmpiftype        integer,
    snmpifname        varchar(96),
    snmpifspeed       bigint,
    snmpifadminstatus integer,
    snmpifoperstatus  integer,
    snmpifalias       varchar(256),
    snmpcollect       varchar(2) default 'N'::character varying,
    snmplastcapsdpoll timestamp with time zone,
    snmppoll          varchar(1) default 'N'::character varying,
    snmplastsnmppoll  timestamp with time zone,
    last_ingress_flow timestamp with time zone,
    last_egress_flow  timestamp with time zone
);

alter table public.snmpinterface
    owner to opennms;

create unique index snmpinterface_nodeid_ifindex_unique_idx
    on public.snmpinterface (nodeid, snmpifindex);

create index snmpinterface_nodeid_idx
    on public.snmpinterface (nodeid);

create index snmpinterface_snmpphysaddr_idx
    on public.snmpinterface (snmpphysaddr);

create table public.ipinterface
(
    id              integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid          integer                                           not null
        constraint fk_nodeid1
            references public.node
            on delete cascade,
    ipaddr          text                                              not null,
    iphostname      varchar(256),
    ismanaged       char,
    ipstatus        integer,
    iplastcapsdpoll timestamp with time zone,
    issnmpprimary   char,
    snmpinterfaceid integer
        constraint snmpinterface_fkey2
            references public.snmpinterface
            on delete set null,
    netmask         varchar(45)
);

alter table public.ipinterface
    owner to opennms;

create index ipinterface_ismanaged_idx
    on public.ipinterface (ismanaged);

create index ipinterface_nodeid_idx
    on public.ipinterface (nodeid);

create index ipinterface_snmpinterfaceid_idx
    on public.ipinterface (snmpinterfaceid);

create unique index ipinterface_nodeid_ipaddr_notzero_idx
    on public.ipinterface (nodeid, ipaddr);

create index ipinterface_nodeid_ipaddr_ismanaged_idx
    on public.ipinterface (nodeid, ipaddr, ismanaged);

create index ipinterface_ipaddr_ismanaged_idx
    on public.ipinterface (ipaddr, ismanaged);

create index ipinterface_ipaddr_idx
    on public.ipinterface (ipaddr);

create table public.ifservices
(
    id            integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    ifindex       integer,
    serviceid     integer                                           not null
        constraint fk_serviceid1
            references public.service
            on delete cascade,
    lastgood      timestamp with time zone,
    lastfail      timestamp with time zone,
    qualifier     char(16),
    status        char,
    source        char,
    notify        char,
    ipinterfaceid integer                                           not null
        constraint ipinterface_fkey
            references public.ipinterface
            on delete cascade
);

alter table public.ifservices
    owner to opennms;

create index ifservices_serviceid_idx
    on public.ifservices (serviceid);

create index ifservicves_ipinterfaceid_idx
    on public.ifservices (ipinterfaceid);

create unique index ifservices_ipinterfaceid_svc_unique
    on public.ifservices (ipinterfaceid, serviceid);

create index ifservices_ipinterfaceid_status
    on public.ifservices (ipinterfaceid, status);

create table public.outages
(
    outageid           integer                  not null
        constraint pk_outageid
            primary key,
    svclosteventid     integer
        constraint fk_eventid1
            references public.events
            on delete cascade,
    svcregainedeventid integer
        constraint fk_eventid2
            references public.events
            on delete cascade,
    iflostservice      timestamp with time zone not null,
    ifregainedservice  timestamp with time zone,
    suppresstime       timestamp with time zone,
    suppressedby       varchar(256),
    ifserviceid        integer                  not null
        constraint ifservices_fkey2
            references public.ifservices
            on delete cascade,
    perspective        text
        constraint fk_outages_perspective
            references public.monitoringlocations
            on update cascade on delete cascade
);

alter table public.outages
    owner to opennms;

create index outages_svclostid_idx
    on public.outages (svclosteventid);

create index outages_svcregainedid_idx
    on public.outages (svcregainedeventid);

create index outages_regainedservice_idx
    on public.outages (ifregainedservice);

create index outages_ifserviceid_idx
    on public.outages (ifserviceid);

create unique index one_outstanding_outage_per_service_and_location_idx1
    on public.outages (ifserviceid)
    where ((perspective IS NULL) AND (ifregainedservice IS NULL));

create unique index one_outstanding_outage_per_service_and_location_idx2
    on public.outages (ifserviceid, perspective)
    where ((NOT (perspective IS NULL)) AND (ifregainedservice IS NULL));

create table public.notifications
(
    notifyid        integer      not null
        constraint pk_notifyid
            primary key,
    textmsg         text         not null,
    subject         text,
    numericmsg      varchar(256),
    pagetime        timestamp with time zone,
    respondtime     timestamp with time zone,
    answeredby      varchar(256),
    nodeid          integer
        constraint fk_nodeid7
            references public.node
            on delete cascade,
    interfaceid     text,
    serviceid       integer,
    queueid         varchar(256),
    eventid         integer
        constraint fk_eventid3
            references public.events
            on delete cascade,
    eventuei        varchar(256) not null,
    notifconfigname text
);

alter table public.notifications
    owner to opennms;

create index notifications_nodeid_idx
    on public.notifications (nodeid);

create index notifications_serviceid_idx
    on public.notifications (serviceid);

create index notifications_eventid_idx
    on public.notifications (eventid);

create index notifications_respondtime_idx
    on public.notifications (respondtime);

create index notifications_answeredby_idx
    on public.notifications (answeredby);

create index notifications_eventuei_idx
    on public.notifications (eventuei);

create index notifications_interfaceid_idx
    on public.notifications (interfaceid);

create table public.usersnotified
(
    id          integer      not null
        constraint pk_usernotificationid
            primary key,
    userid      varchar(256) not null,
    notifyid    integer
        constraint fk_notifid2
            references public.notifications
            on delete cascade,
    notifytime  timestamp with time zone,
    media       varchar(32),
    contactinfo varchar(64),
    autonotify  char
);

alter table public.usersnotified
    owner to opennms;

create index userid_notifyid_idx
    on public.usersnotified (userid, notifyid);

create index usersnotified_notifyid_idx
    on public.usersnotified (notifyid);

create table public.alarms
(
    alarmid               integer           not null
        constraint pk_alarmid
            primary key,
    eventuei              varchar(256)      not null,
    nodeid                integer
        constraint fk_alarms_nodeid
            references public.node
            on delete cascade,
    ipaddr                text,
    serviceid             integer,
    reductionkey          text,
    alarmtype             integer,
    counter               integer           not null,
    severity              integer           not null,
    lasteventid           integer
        constraint fk_eventidak2
            references public.events
            on delete cascade,
    firsteventtime        timestamp with time zone,
    lasteventtime         timestamp with time zone,
    firstautomationtime   timestamp with time zone,
    lastautomationtime    timestamp with time zone,
    description           text,
    logmsg                text,
    operinstruct          text,
    tticketid             varchar(128),
    tticketstate          integer,
    mouseovertext         varchar(64),
    suppresseduntil       timestamp with time zone,
    suppresseduser        varchar(256),
    suppressedtime        timestamp with time zone,
    alarmackuser          varchar(256),
    alarmacktime          timestamp with time zone,
    managedobjectinstance varchar(512),
    managedobjecttype     varchar(512),
    applicationdn         varchar(512),
    ossprimarykey         varchar(512),
    x733alarmtype         varchar(31),
    x733probablecause     integer default 0 not null,
    qosalarmstate         varchar(31),
    clearkey              text,
    ifindex               integer,
    stickymemo            integer
        constraint "fk_stickyMemo"
            references public.memos
            on delete cascade,
    systemid              text              not null
        constraint fk_alarms_systemid
            references public.monitoringsystems
            on delete cascade
);

alter table public.alarms
    owner to opennms;

create index alarm_uei_idx
    on public.alarms (eventuei);

create index alarm_nodeid_idx
    on public.alarms (nodeid);

create index alarm_app_dn
    on public.alarms (applicationdn);

create index alarm_oss_primary_key
    on public.alarms (ossprimarykey);

create index alarm_eventid_idx
    on public.alarms (lasteventid);

create index alarm_lasteventtime_idx
    on public.alarms (lasteventtime);

create unique index alarm_reductionkey_idx
    on public.alarms (reductionkey);

create index alarm_clearkey_idx
    on public.alarms (clearkey);

create index alarm_reduction_idx
    on public.alarms (alarmid, eventuei, systemid, nodeid, serviceid, reductionkey);

create table public.alarm_attributes
(
    alarmid        integer not null
        constraint fk_alarmid1
            references public.alarms
            on delete cascade,
    attributename  varchar(63),
    attributevalue varchar(255)
);

alter table public.alarm_attributes
    owner to opennms;

create index alarm_attributes_idx
    on public.alarm_attributes (alarmid);

create unique index alarm_attributes_aan_idx
    on public.alarm_attributes (alarmid, attributename);

create table public.assets
(
    id                    integer default nextval('opennmsnxtid'::regclass) not null
        constraint pk_assetid
            primary key,
    nodeid                integer
        constraint fk_nodeid5
            references public.node
            on delete cascade,
    category              text                                              not null,
    manufacturer          text,
    vendor                text,
    modelnumber           text,
    serialnumber          text,
    description           text,
    circuitid             text,
    assetnumber           text,
    operatingsystem       text,
    rack                  text,
    slot                  text,
    port                  text,
    region                text,
    division              text,
    department            text,
    address1              text,
    address2              text,
    city                  text,
    state                 text,
    zip                   text,
    building              text,
    floor                 text,
    room                  text,
    vendorphone           text,
    vendorfax             text,
    vendorassetnumber     text,
    userlastmodified      varchar(20)                                       not null,
    lastmodifieddate      timestamp with time zone                          not null,
    dateinstalled         varchar(64),
    lease                 text,
    leaseexpires          varchar(64),
    supportphone          text,
    maintcontract         text,
    maintcontractexpires  varchar(64),
    displaycategory       text,
    notifycategory        text,
    pollercategory        text,
    thresholdcategory     text,
    comment               text,
    managedobjectinstance text,
    managedobjecttype     text,
    username              text,
    password              text,
    enable                text,
    autoenable            char,
    connection            varchar(32),
    cpu                   text,
    ram                   text,
    storagectrl           text,
    hdd1                  text,
    hdd2                  text,
    hdd3                  text,
    hdd4                  text,
    hdd5                  text,
    hdd6                  text,
    numpowersupplies      varchar(1),
    inputpower            varchar(11),
    additionalhardware    text,
    admin                 text,
    snmpcommunity         varchar(32),
    rackunitheight        varchar(2),
    country               text,
    longitude             double precision,
    latitude              double precision
);

alter table public.assets
    owner to opennms;

create index assets_nodeid_idx
    on public.assets (nodeid);

create index assets_an_idx
    on public.assets (assetnumber);

create unique index assets_unique_nodeid_idx
    on public.assets (nodeid);

create table public.category_node
(
    categoryid integer not null
        constraint categoryid_fkey1
            references public.categories
            on delete cascade,
    nodeid     integer not null
        constraint nodeid_fkey1
            references public.node
            on delete cascade
);

alter table public.category_node
    owner to opennms;

create index catid_idx
    on public.category_node (categoryid);

create index catnode_idx
    on public.category_node (nodeid);

create unique index catenode_unique_idx
    on public.category_node (categoryid, nodeid);

create table public.pathoutage
(
    nodeid                  integer not null
        constraint fk_nodeid8
            references public.node
            on delete cascade,
    criticalpathip          text    not null,
    criticalpathservicename varchar(255)
);

alter table public.pathoutage
    owner to opennms;

create unique index pathoutage_nodeid
    on public.pathoutage (nodeid);

create index pathoutage_criticalpathservicename_idx
    on public.pathoutage (criticalpathservicename);

create index pathoutage_criticalpathip
    on public.pathoutage (criticalpathip);

create table public.application_service_map
(
    appid       integer not null
        constraint applicationid_fkey1
            references public.applications
            on delete cascade,
    ifserviceid integer not null
        constraint ifservices_fkey3
            references public.ifservices
            on delete cascade
);

alter table public.application_service_map
    owner to opennms;

create index appid_idx
    on public.application_service_map (appid);

create index ifserviceid_idx
    on public.application_service_map (ifserviceid);

create unique index appid_ifserviceid_idex
    on public.application_service_map (appid, ifserviceid);

create table public.inventory
(
    nodeid       integer                  not null
        constraint fk_ia_nodeid7
            references public.node
            on delete cascade,
    name         varchar(30)              not null,
    createtime   timestamp with time zone not null,
    lastpolltime timestamp with time zone not null,
    pathtofile   varchar(256)             not null,
    status       char                     not null
);

alter table public.inventory
    owner to opennms;

create index inventory_nodeid_name_idx
    on public.inventory (nodeid, name);

create index inventory_nodeid_idx
    on public.inventory (nodeid);

create index inventory_lastpolltime_idx
    on public.inventory (lastpolltime);

create index inventory_status_idx
    on public.inventory (status);

create table public.lldpelement
(
    id                   integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid               integer                                           not null
        constraint fk_lldpelement_nodeid
            references public.node
            on delete cascade,
    lldpchassisid        text                                              not null,
    lldpchassisidsubtype integer                                           not null,
    lldpsysname          text                                              not null,
    lldpnodecreatetime   timestamp with time zone                          not null,
    lldpnodelastpolltime timestamp with time zone                          not null
);

alter table public.lldpelement
    owner to opennms;

create index lldp_chassis_idx
    on public.lldpelement (lldpchassisid);

create index lldp_sysname_idx
    on public.lldpelement (lldpsysname);

create unique index lldpelement_unique_nodeid_idx
    on public.lldpelement (nodeid);

create table public.lldplink
(
    id                      integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                  integer                                           not null
        constraint fk_lldplink_nodeid
            references public.node
            on delete cascade,
    lldpremindex            integer                                           not null,
    lldpportid              text                                              not null,
    lldpportidsubtype       integer                                           not null,
    lldpportdescr           text                                              not null,
    lldpportifindex         integer,
    lldpremchassisid        text                                              not null,
    lldpremchassisidsubtype integer                                           not null,
    lldpremsysname          text                                              not null,
    lldpremportid           text                                              not null,
    lldpremportidsubtype    integer                                           not null,
    lldpremportdescr        text                                              not null,
    lldplinkcreatetime      timestamp with time zone                          not null,
    lldplinklastpolltime    timestamp with time zone                          not null,
    lldpremlocalportnum     integer default 1                                 not null,
    constraint lldplink_pk_idx
        unique (nodeid, lldpremlocalportnum, lldpremindex)
);

alter table public.lldplink
    owner to opennms;

create index lldplink_nodeid_idx
    on public.lldplink (nodeid);

create index lldplink_lastpoll_idx
    on public.lldplink (lldplinklastpolltime);

create table public.ospfelement
(
    id                   integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid               integer                                           not null
        constraint fk_ospfelement_nodeid
            references public.node
            on delete cascade,
    ospfrouterid         varchar(16)                                       not null,
    ospfadminstat        integer                                           not null,
    ospfversionnumber    integer                                           not null,
    ospfbdrrtrstatus     integer                                           not null,
    ospfasbdrrtrstatus   integer                                           not null,
    ospfrouteridnetmask  varchar(16)                                       not null,
    ospfrouteridifindex  integer                                           not null,
    ospfnodecreatetime   timestamp with time zone                          not null,
    ospfnodelastpolltime timestamp with time zone                          not null
);

alter table public.ospfelement
    owner to opennms;

create index ospfelement_routerid_idx
    on public.ospfelement (ospfrouterid);

create unique index ospfelement_unique_nodeid_idx
    on public.ospfelement (nodeid);

create table public.ospflink
(
    id                      integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                  integer                                           not null
        constraint fk_ospflink_nodeid
            references public.node
            on delete cascade,
    ospfipaddr              varchar(16),
    ospfipmask              varchar(16),
    ospfaddresslessindex    integer,
    ospfifindex             integer,
    ospfremrouterid         varchar(16)                                       not null,
    ospfremipaddr           varchar(16)                                       not null,
    ospfremaddresslessindex integer                                           not null,
    ospflinkcreatetime      timestamp with time zone                          not null,
    ospflinklastpolltime    timestamp with time zone                          not null,
    ospfifareaid            varchar(16)
);

alter table public.ospflink
    owner to opennms;

create index ospflink_nodeid_idx
    on public.ospflink (nodeid);

create index ospflink_lastpoll_idx
    on public.ospflink (ospflinklastpolltime);

create index ospflink_pk_idx
    on public.ospflink (nodeid, ospfremrouterid, ospfremipaddr, ospfremaddresslessindex);

create table public.isiselement
(
    id                   integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid               integer                                           not null
        constraint fk_isiselement_nodeid
            references public.node
            on delete cascade,
    isissysid            varchar(32)                                       not null,
    isissysadminstate    integer                                           not null,
    isisnodecreatetime   timestamp with time zone                          not null,
    isisnodelastpolltime timestamp with time zone                          not null
);

alter table public.isiselement
    owner to opennms;

create index isiselement_sysid_idx
    on public.isiselement (isissysid);

create unique index isiselement_unique_nodeid_idx
    on public.isiselement (nodeid);

create table public.isislink
(
    id                         integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                     integer                                           not null
        constraint fk_isislink_nodeid
            references public.node
            on delete cascade,
    isiscircindex              integer                                           not null,
    isisisadjindex             integer                                           not null,
    isiscircifindex            integer,
    isiscircadminstate         integer,
    isisisadjstate             integer                                           not null,
    isisisadjneighsnpaaddress  varchar(80)                                       not null,
    isisisadjneighsystype      integer                                           not null,
    isisisadjneighsysid        varchar(32)                                       not null,
    isisisadjnbrextendedcircid integer                                           not null,
    isislinkcreatetime         timestamp with time zone                          not null,
    isislinklastpolltime       timestamp with time zone                          not null
);

alter table public.isislink
    owner to opennms;

create index isislink_nodeid_idx
    on public.isislink (nodeid);

create index isislink_lastpoll_idx
    on public.isislink (isislinklastpolltime);

create index isislink_pk_idx
    on public.isislink (nodeid, isiscircindex, isisisadjindex);

create table public.ipnettomedia
(
    id            integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    netaddress    text                                              not null,
    physaddress   varchar(32)                                       not null,
    sourcenodeid  integer                                           not null
        constraint fk_ipnettomedia_sourcenodeid
            references public.node
            on delete cascade,
    sourceifindex integer                                           not null,
    createtime    timestamp with time zone                          not null,
    lastpolltime  timestamp with time zone                          not null,
    nodeid        integer
        constraint fk_ipnettomedia_nodeid
            references public.node
            on delete cascade,
    ifindex       integer,
    port          text
);

alter table public.ipnettomedia
    owner to opennms;

create index ipnettomedia_nodeid_idx
    on public.ipnettomedia (sourcenodeid);

create index ipnettomedia_lastpoll_idx
    on public.ipnettomedia (lastpolltime);

create index ipnettomedia_pk_idx
    on public.ipnettomedia (netaddress, physaddress);

create table public.bridgeelement
(
    id                       integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                   integer                                           not null
        constraint fk_bridgeelement_nodeid
            references public.node
            on delete cascade,
    basebridgeaddress        varchar(12)                                       not null,
    basenumports             integer                                           not null,
    basetype                 integer                                           not null,
    vlan                     integer,
    vlanname                 text,
    stpprotocolspecification integer,
    stppriority              integer,
    stpdesignatedroot        varchar(16),
    stprootcost              integer,
    stprootport              integer,
    bridgenodecreatetime     timestamp with time zone                          not null,
    bridgenodelastpolltime   timestamp with time zone                          not null
);

alter table public.bridgeelement
    owner to opennms;

create index bridgeelement_nodeid_idx
    on public.bridgeelement (nodeid);

create index bridgeelement_lastpoll_idx
    on public.bridgeelement (bridgenodelastpolltime);

create index bridgeelement_pk_idx
    on public.bridgeelement (nodeid, basebridgeaddress);

create table public.bridgemaclink
(
    id                        integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                    integer                                           not null
        constraint fk_bridgemaclink_nodeid
            references public.node
            on delete cascade,
    bridgeport                integer                                           not null,
    bridgeportifindex         integer,
    bridgeportifname          text,
    vlan                      integer,
    macaddress                varchar(12)                                       not null,
    bridgemaclinkcreatetime   timestamp with time zone                          not null,
    bridgemaclinklastpolltime timestamp with time zone                          not null,
    linktype                  integer                                           not null
);

alter table public.bridgemaclink
    owner to opennms;

create index bridgemaclink_nodeid_idx
    on public.bridgemaclink (nodeid);

create index bridgemaclink_lastpoll_idx
    on public.bridgemaclink (bridgemaclinklastpolltime);

create index bridgemaclink_pk_idx1
    on public.bridgemaclink (nodeid, bridgeport);

create index bridgemaclink_pk_idx2
    on public.bridgemaclink (macaddress);

create table public.bridgestplink
(
    id                        integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                    integer                                           not null
        constraint fk_bridgestplink_nodeid
            references public.node
            on delete cascade,
    stpport                   integer                                           not null,
    stpportpriority           integer                                           not null,
    stpportstate              integer                                           not null,
    stpportenable             integer                                           not null,
    stpportpathcost           integer                                           not null,
    stpportifindex            integer,
    stpportifname             text,
    vlan                      integer,
    designatedroot            varchar(16)                                       not null,
    designatedcost            integer                                           not null,
    designatedbridge          varchar(16)                                       not null,
    designatedport            varchar(4)                                        not null,
    bridgestplinkcreatetime   timestamp with time zone                          not null,
    bridgestplinklastpolltime timestamp with time zone                          not null
);

alter table public.bridgestplink
    owner to opennms;

create index bridgestplink_nodeid_idx
    on public.bridgestplink (nodeid);

create index bridgestplink_lastpoll_idx
    on public.bridgestplink (bridgestplinklastpolltime);

create index bridgestplink_pk_idx1
    on public.bridgestplink (nodeid, stpport);

create index bridgestplink_pk_idx2
    on public.bridgestplink (designatedbridge);

create table public.bridgebridgelink
(
    id                           integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                       integer                                           not null
        constraint fk_bridgebridgelink_nodeid
            references public.node
            on delete cascade,
    bridgeport                   integer,
    bridgeportifindex            integer,
    bridgeportifname             text,
    vlan                         integer,
    designatednodeid             integer                                           not null
        constraint fk_bridgebridgelink_designatednodeid
            references public.node
            on delete cascade,
    designatedbridgeport         integer,
    designatedbridgeportifindex  integer,
    designatedbridgeportifname   text,
    designatedvlan               integer,
    bridgebridgelinkcreatetime   timestamp with time zone                          not null,
    bridgebridgelinklastpolltime timestamp with time zone                          not null
);

alter table public.bridgebridgelink
    owner to opennms;

create index bridgebridgelink_nodeid_idx
    on public.bridgebridgelink (nodeid);

create index bridgebridgelink_lastpoll_idx
    on public.bridgebridgelink (bridgebridgelinklastpolltime);

create index bridgebridgelink_pk_idx
    on public.bridgebridgelink (nodeid, bridgeport);

create table public.cdpelement
(
    id                      integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                  integer                                           not null
        constraint fk_cdpelement_nodeid
            references public.node
            on delete cascade,
    cdpglobalrun            integer                                           not null,
    cdpglobaldeviceid       text                                              not null,
    cdpnodecreatetime       timestamp with time zone                          not null,
    cdpnodelastpolltime     timestamp with time zone                          not null,
    cdpglobaldeviceidformat integer
);

alter table public.cdpelement
    owner to opennms;

create index cdp_deviceid_idx
    on public.cdpelement (cdpglobaldeviceid);

create unique index cdpelement_unique_nodeid_idx
    on public.cdpelement (nodeid);

create table public.cdplink
(
    id                     integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid                 integer                                           not null
        constraint fk_cdplink_nodeid
            references public.node
            on delete cascade,
    cdpcacheifindex        integer                                           not null,
    cdpinterfacename       text,
    cdpcacheaddresstype    integer                                           not null,
    cdpcacheaddress        text                                              not null,
    cdpcacheversion        text                                              not null,
    cdpcachedeviceid       text                                              not null,
    cdpcachedeviceport     text                                              not null,
    cdpcachedeviceplatform text                                              not null,
    cdplinkcreatetime      timestamp with time zone                          not null,
    cdplinklastpolltime    timestamp with time zone                          not null,
    cdpcachedeviceindex    integer                                           not null
);

alter table public.cdplink
    owner to opennms;

create index cdplink_nodeid_idx
    on public.cdplink (nodeid);

create index cdplink_lastpoll_idx
    on public.cdplink (cdplinklastpolltime);

create index cdplink_pk_idx
    on public.cdplink (nodeid, cdpcachedeviceid, cdpcachedeviceport);

create index cdplink_address_idx
    on public.cdplink (cdpcacheaddress);

create index cdplink_pk_new_idx
    on public.cdplink (nodeid, cdpcacheifindex, cdpcachedeviceindex);

create table public.hwentity
(
    id                      integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    parentid                integer
        constraint fk_hwentity_parent
            references public.hwentity
            on delete cascade,
    nodeid                  integer
        constraint fk_hwentity_node
            references public.node
            on delete cascade,
    entphysicalindex        integer                                           not null,
    entphysicalparentrelpos integer,
    entphysicalname         varchar(128),
    entphysicaldescr        varchar(128),
    entphysicalalias        varchar(128),
    entphysicalvendortype   varchar(128),
    entphysicalclass        varchar(128),
    entphysicalmfgname      varchar(128),
    entphysicalmodelname    varchar(128),
    entphysicalhardwarerev  varchar(128),
    entphysicalfirmwarerev  varchar(128),
    entphysicalsoftwarerev  varchar(128),
    entphysicalserialnum    varchar(128),
    entphysicalassetid      varchar(128),
    entphysicalisfru        boolean,
    entphysicalmfgdate      timestamp with time zone,
    entphysicaluris         varchar(256)
);

alter table public.hwentity
    owner to opennms;

create index hwentity_entphysicalindex_idx
    on public.hwentity (entphysicalindex);

create index hwentity_nodeid_idx
    on public.hwentity (nodeid);

create table public.hwentityattribute
(
    id             integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    hwentityid     integer                                           not null
        constraint fk_hwentity_hwentityattribute
            references public.hwentity
            on delete cascade,
    hwattribtypeid integer                                           not null
        constraint fk_hwentityattribute_hwentityattributetype
            references public.hwentityattributetype
            on delete cascade,
    attribvalue    varchar(256)                                      not null
);

alter table public.hwentityattribute
    owner to opennms;

create unique index hwentityattribute_unique_idx
    on public.hwentityattribute (hwentityid, hwattribtypeid);

create table public.requisitioned_categories
(
    id         integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid     integer                                           not null
        constraint requisitioned_nodeid_fkey
            references public.node
            on delete cascade,
    categoryid integer                                           not null
        constraint requisitioned_categoryid_fkey
            references public.categories
            on delete cascade
);

alter table public.requisitioned_categories
    owner to opennms;

create index requisitioned_category_node_unique_idx
    on public.requisitioned_categories (nodeid, categoryid);

create table public.monitoringlocationstags
(
    monitoringlocationid text not null
        constraint monitoringlocationstags_fkey
            references public.monitoringlocations
            on update cascade on delete cascade,
    tag                  text not null
);

alter table public.monitoringlocationstags
    owner to opennms;

create index monitoringlocationstags_id_idx
    on public.monitoringlocationstags (monitoringlocationid);

create unique index monitoringlocationstags_id_pkg_idx
    on public.monitoringlocationstags (monitoringlocationid, tag);

create table public.bsm_reduce
(
    id                 integer     not null
        primary key,
    type               varchar(32) not null,
    threshold          double precision,
    threshold_severity integer,
    base               double precision
);

alter table public.bsm_reduce
    owner to opennms;

create table public.bsm_service
(
    id            integer not null
        constraint bsm_services_pkey
            primary key,
    bsm_reduce_id integer not null
        constraint fk_bsm_service_reduce_id
            references public.bsm_reduce,
    name          varchar(255)
        unique
);

alter table public.bsm_service
    owner to opennms;

create table public.bsm_service_attributes
(
    bsm_service_id integer      not null
        constraint fk_bsm_service_attributes_service_id
            references public.bsm_service,
    key            varchar(255) not null,
    value          text         not null,
    primary key (bsm_service_id, key)
);

alter table public.bsm_service_attributes
    owner to opennms;

create table public.bsm_map
(
    id       integer     not null
        primary key,
    type     varchar(32) not null,
    severity integer
);

alter table public.bsm_map
    owner to opennms;

create table public.bsm_service_edge
(
    id             integer not null
        primary key,
    enabled        boolean not null,
    weight         integer not null,
    bsm_map_id     integer not null
        constraint fk_bsm_service_edge_map_id
            references public.bsm_map,
    bsm_service_id integer not null
        constraint fk_bsm_service_edge_service_id
            references public.bsm_service
);

alter table public.bsm_service_edge
    owner to opennms;

create table public.bsm_service_ifservices
(
    id           integer not null
        primary key
        constraint fk_bsm_service_ifservices_edge_id
            references public.bsm_service_edge
            on delete cascade,
    ifserviceid  integer not null
        constraint fk_bsm_service_ifservices_ifserviceid
            references public.ifservices
            on delete cascade,
    friendlyname varchar(255)
);

alter table public.bsm_service_ifservices
    owner to opennms;

create table public.bsm_service_reductionkeys
(
    id           integer not null
        primary key
        constraint fk_bsm_service_reductionkeys_edge_id
            references public.bsm_service_edge,
    reductionkey text    not null,
    friendlyname varchar(255)
);

alter table public.bsm_service_reductionkeys
    owner to opennms;

create table public.bsm_service_children
(
    id                   integer not null
        primary key
        constraint fk_bsm_service_children_edge_id
            references public.bsm_service_edge
            on delete cascade,
    bsm_service_child_id integer not null
        constraint fk_bsm_service_child_service_id
            references public.bsm_service
            on delete cascade
);

alter table public.bsm_service_children
    owner to opennms;

create table public.topo_layout
(
    id        text                     not null
        primary key,
    created   timestamp with time zone not null,
    creator   text                     not null,
    updated   timestamp with time zone not null,
    updator   text                     not null,
    last_used timestamp with time zone
);

alter table public.topo_layout
    owner to opennms;

create table public.topo_vertex_position
(
    id               integer not null
        primary key,
    x                integer not null,
    y                integer not null,
    vertex_namespace text    not null,
    vertex_id        text    not null
);

alter table public.topo_vertex_position
    owner to opennms;

create table public.topo_layout_vertex_positions
(
    vertex_position_id integer not null
        constraint fk_topo_layout_vertex_positions_vertex_position_id
            references public.topo_vertex_position
            on delete cascade,
    layout_id          text    not null
        constraint fk_topo_layout_vertex_positions_layout_id
            references public.topo_layout
            on delete cascade
);

alter table public.topo_layout_vertex_positions
    owner to opennms;

create table public.qrtz_simprop_triggers
(
    sched_name    varchar(120) not null,
    trigger_name  varchar(200) not null,
    trigger_group varchar(200) not null,
    str_prop_1    varchar(512),
    str_prop_2    varchar(512),
    str_prop_3    varchar(512),
    int_prop_1    integer,
    int_prop_2    integer,
    long_prop_1   bigint,
    long_prop_2   bigint,
    dec_prop_1    numeric(13, 4),
    dec_prop_2    numeric(13, 4),
    bool_prop_1   boolean,
    bool_prop_2   boolean,
    constraint pk_qrtz_simprop_triggers
        primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_simprop_triggers_triggers
        foreign key (sched_name, trigger_name, trigger_group) references public.qrtz_triggers
);

alter table public.qrtz_simprop_triggers
    owner to opennms;

create table public.event_parameters
(
    eventid  integer      not null
        constraint fk_eventparameterseventid
            references public.events
            on delete cascade,
    name     text         not null,
    value    text         not null,
    type     varchar(256) not null,
    position integer default 0,
    constraint pk_eventparameters
        primary key (eventid, name)
);

alter table public.event_parameters
    owner to opennms;

create table public.classification_groups
(
    id          integer not null
        primary key,
    name        text    not null
        unique,
    description text,
    readonly    boolean,
    enabled     boolean,
    position    integer not null
);

alter table public.classification_groups
    owner to opennms;

create table public.classification_rules
(
    id              integer               not null
        primary key,
    name            text                  not null,
    src_address     text,
    src_port        text,
    exporter_filter text,
    dst_address     text,
    dst_port        text,
    protocol        text,
    position        integer               not null,
    groupid         integer               not null
        constraint fk_classification_rules_groupid
            references public.classification_groups
            on delete cascade,
    omnidirectional boolean default false not null,
    constraint classification_rules_src_address_src_port_exporter_filter_d_key
        unique (src_address, src_port, exporter_filter, dst_address, dst_port, protocol, groupid)
);

alter table public.classification_rules
    owner to opennms;

create table public.hwentityalias
(
    id         integer default nextval('opennmsnxtid'::regclass) not null
        primary key,
    hwentityid integer                                           not null
        constraint fk_hwentity_hwentityalias
            references public.hwentity
            on delete cascade,
    index      integer                                           not null,
    oid        text                                              not null
);

alter table public.hwentityalias
    owner to opennms;

create unique index hwentityalias_unique_idx
    on public.hwentityalias (hwentityid, index);

create table public.alarm_situations
(
    id               integer                  not null
        primary key,
    situation_id     integer                  not null
        constraint fk_alarm_situations_situation_id
            references public.alarms
            on delete cascade,
    related_alarm_id integer                  not null
        constraint fk_alarm_situations_alarm_id
            references public.alarms
            on delete cascade,
    mapped_time      timestamp with time zone not null
);

alter table public.alarm_situations
    owner to opennms;

create table public.bsm_service_applications
(
    id            integer not null
        primary key
        constraint fk_bsm_service_applications_edge_id
            references public.bsm_service_edge
            on delete cascade,
    applicationid integer not null
        constraint fk_bsm_service_applications_applicationid
            references public.applications
            on delete cascade
);

alter table public.bsm_service_applications
    owner to opennms;

create table public.node_metadata
(
    id      integer not null
        constraint fk_node_metadata_id
            references public.node
            on delete cascade,
    context text    not null,
    key     text    not null,
    value   text    not null,
    primary key (id, context, key)
);

alter table public.node_metadata
    owner to opennms;

create table public.ipinterface_metadata
(
    id      integer not null
        constraint fk_ipinterface_metadata_id
            references public.ipinterface
            on delete cascade,
    context text    not null,
    key     text    not null,
    value   text    not null,
    primary key (id, context, key)
);

alter table public.ipinterface_metadata
    owner to opennms;

create table public.ifservices_metadata
(
    id      integer not null
        constraint fk_ifservices_metadata_id
            references public.ifservices
            on delete cascade,
    context text    not null,
    key     text    not null,
    value   text    not null,
    primary key (id, context, key)
);

alter table public.ifservices_metadata
    owner to opennms;

create table public.user_defined_links
(
    id                integer not null
        primary key,
    node_id_a         integer not null
        constraint fk_user_defined_links_node_id_a
            references public.node
            on delete cascade,
    node_id_z         integer not null
        constraint fk_user_defined_links_node_id_z
            references public.node
            on delete cascade,
    component_label_a text,
    component_label_z text,
    link_id           text    not null,
    link_label        text,
    owner             text    not null
);

alter table public.user_defined_links
    owner to opennms;

create table public.endpoints_grafana
(
    id              integer not null
        primary key,
    uid             text    not null
        unique,
    url             text    not null,
    api_key         text    not null,
    description     text,
    connect_timeout integer,
    read_timeout    integer
);

alter table public.endpoints_grafana
    owner to opennms;

create table public.kvstore_jsonb
(
    key          text      not null,
    context      text      not null,
    last_updated timestamp not null,
    expires_at   timestamp,
    value        jsonb     not null,
    constraint pk_kvstore_jsonb
        primary key (key, context)
);

alter table public.kvstore_jsonb
    owner to opennms;

create table public.kvstore_bytea
(
    key          text      not null,
    context      text      not null,
    last_updated timestamp not null,
    expires_at   timestamp,
    value        bytea     not null,
    constraint pk_kvstore_bytea
        primary key (key, context)
);

alter table public.kvstore_bytea
    owner to opennms;

create table public.graph_focus
(
    id        bigint not null
        primary key,
    type      text   not null,
    selection text
);

comment on table public.graph_focus is 'focus for graph elements';

comment on column public.graph_focus.selection is 'optional list of vertex ids defining the (custom) focus';

alter table public.graph_focus
    owner to opennms;

create table public.graph_elements
(
    id                      bigint       not null
        primary key,
    type                    varchar(25)  not null,
    namespace               varchar(200) not null,
    source_vertex_namespace varchar(200),
    source_vertex_id        text,
    target_vertex_namespace varchar(200),
    target_vertex_id        text,
    focus_id                bigint
        constraint fk_graph_focus_graph_id
            references public.graph_focus
            on delete cascade
);

comment on table public.graph_elements is 'contains vertices, edges, graphs and containers';

alter table public.graph_elements
    owner to opennms;

create table public.graph_attributes
(
    id         bigint not null
        primary key,
    name       text   not null,
    type       text   not null,
    value      text,
    element_id bigint
        constraint fk_graph_attributes_element_id
            references public.graph_elements
            on delete cascade,
    unique (name, element_id)
);

comment on table public.graph_attributes is 'attributes for each graph_element';

comment on column public.graph_attributes.name is 'the property name';

comment on column public.graph_attributes.type is 'the property''s type';

comment on column public.graph_attributes.value is 'the text representation of the property''s value';

comment on column public.graph_attributes.element_id is 'the element this property is associated with';

alter table public.graph_attributes
    owner to opennms;

create table public.graph_element_relations
(
    parent_id bigint not null
        constraint fk_graph_element_relations_parent_id
            references public.graph_elements
            on delete cascade,
    child_id  bigint not null
        constraint fk_graph_element_relations_child_id
            references public.graph_elements
            on delete cascade,
    primary key (parent_id, child_id)
);

comment on table public.graph_element_relations is 'Association for graph -> vertices/edge and container -> graph relation';

comment on column public.graph_element_relations.parent_id is 'the owning side of the relation (e.g. graph or container)';

comment on column public.graph_element_relations.child_id is 'the ''child'' side of the relation (e.g. vertex/edge or graph)';

alter table public.graph_element_relations
    owner to opennms;

create table public.application_perspective_location_map
(
    appid                integer not null
        constraint appid_fkey
            references public.applications
            on delete cascade,
    monitoringlocationid text    not null
        constraint "monitoringLocationid_fkey"
            references public.monitoringlocations
            on delete cascade
);

comment on table public.application_perspective_location_map is 'locations from where an application should be monitored';

alter table public.application_perspective_location_map
    owner to opennms;

create index application_perspective_location_map_appid_idx
    on public.application_perspective_location_map (appid);

create index monitoringlocationid_idx
    on public.application_perspective_location_map (monitoringlocationid);

create unique index appid_monitoringlocationid_idx
    on public.application_perspective_location_map (appid, monitoringlocationid);

create table public.bmp_collectors
(
    id            bigint                                       not null
        primary key,
    hash_id       varchar(36)                                  not null,
    state         varchar(8) default 'DOWN'::character varying not null,
    admin_id      varchar(64)                                  not null,
    routers       varchar(4096),
    routers_count smallint   default 0,
    last_updated  timestamp                                    not null,
    name          varchar(200),
    ip_address    varchar(40)
);

comment on table public.bmp_collectors is 'bmp collectors';

alter table public.bmp_collectors
    owner to opennms;

create table public.bmp_routers
(
    id                bigint                                       not null
        primary key,
    hash_id           varchar(36)                                  not null,
    name              varchar(200)                                 not null,
    ip_address        varchar(40)                                  not null,
    router_as         bigint,
    last_updated      timestamp                                    not null,
    description       varchar(255),
    state             varchar(8) default 'DOWN'::character varying not null,
    is_passive        boolean    default false,
    term_reason_code  integer,
    term_reason_text  varchar(255),
    term_data         text,
    init_data         text,
    geo_ip_start      varchar(40),
    collector_hash_id varchar(36)                                  not null,
    bgp_id            varchar(40),
    connection_count  integer    default 0
);

comment on table public.bmp_routers is 'bmp routers';

alter table public.bmp_routers
    owner to opennms;

create index bmp_routers_name
    on public.bmp_routers (name);

create index bmp_routers_ip_address
    on public.bmp_routers (ip_address);

create table public.bmp_peers
(
    id                  bigint                                       not null
        primary key,
    hash_id             varchar(36)                                  not null,
    router_hash_id      varchar(36)                                  not null,
    peer_rd             varchar(32)                                  not null,
    is_ipv4             boolean    default true                      not null,
    peer_addr           varchar(40)                                  not null,
    name                varchar(200),
    peer_bgp_id         varchar(40),
    peer_asn            bigint                                       not null,
    state               varchar(8) default 'DOWN'::character varying not null,
    is_l3vpn_peer       boolean    default false                     not null,
    last_updated        timestamp                                    not null,
    is_pre_policy       boolean    default true                      not null,
    geo_ip_start        varchar(40),
    local_ip            varchar(40),
    local_bgp_id        varchar(40),
    local_port          integer,
    local_hold_time     bigint,
    local_asn           bigint,
    remote_port         integer,
    remote_hold_time    bigint,
    sent_capabilities   varchar(4096),
    recv_capabilities   varchar(4096),
    bmp_reason          smallint,
    bgp_err_code        smallint,
    bgp_err_subcode     smallint,
    error_text          varchar(255),
    is_loc_rib          boolean    default false                     not null,
    is_loc_rib_filtered boolean    default false                     not null,
    table_name          varchar(255)
);

comment on table public.bmp_peers is 'bmp peers';

alter table public.bmp_peers
    owner to opennms;

create index bmp_peers_peer_addr
    on public.bmp_peers (peer_addr);

create index bmp_peers_name
    on public.bmp_peers (name);

create index bmp_peers_peer_asn
    on public.bmp_peers (peer_asn);

create index bmp_peers_router_hash_id
    on public.bmp_peers (router_hash_id);

create table public.bmp_base_attributes
(
    id                   bigint        not null
        primary key,
    hash_id              varchar(36)   not null,
    peer_hash_id         varchar(36)   not null,
    origin               varchar(16)   not null,
    as_path              varchar(8192) not null,
    as_path_count        smallint default 0,
    origin_as            bigint,
    next_hop             varchar(40),
    med                  bigint,
    local_pref           bigint,
    aggregator           varchar(64),
    community_list       varchar(6000),
    ext_community_list   varchar(2048),
    large_community_list varchar(3000),
    cluster_list         varchar(2048),
    is_atomic_agg        boolean  default false,
    is_nexthop_ipv4      boolean  default true,
    last_updated         timestamp     not null,
    originator_id        varchar(40)
);

comment on table public.bmp_base_attributes is 'bmp base attributes';

alter table public.bmp_base_attributes
    owner to opennms;

create index bmp_base_attributes_origin_as
    on public.bmp_base_attributes (origin_as);

create index bmp_base_attributes_as_path_count
    on public.bmp_base_attributes (as_path_count);

create index bmp_base_attributes_as_path
    on public.bmp_base_attributes (as_path);

create index bmp_base_attributes_peer_hash_id
    on public.bmp_base_attributes (peer_hash_id);

create table public.bmp_ip_ribs
(
    id                    bigint                not null
        primary key,
    hash_id               varchar(36)           not null,
    peer_hash_id          varchar(36)           not null,
    base_attr_hash_id     varchar(36),
    is_ipv4               boolean               not null,
    origin_as             bigint,
    prefix                varchar(40)           not null,
    prefix_len            smallint              not null,
    last_updated          timestamp             not null,
    first_added_timestamp timestamp             not null,
    is_withdrawn          boolean default false not null,
    prefix_bits           varchar(128),
    path_id               bigint,
    labels                varchar(255),
    is_pre_policy         boolean default true  not null,
    is_adj_ribin          boolean default true  not null
);

comment on table public.bmp_ip_ribs is 'bmp unicast prefixes';

alter table public.bmp_ip_ribs
    owner to opennms;

create index bmp_ip_ribs_peer_hash_id
    on public.bmp_ip_ribs (peer_hash_id);

create index bmp_ip_ribs_base_attr_hash_id
    on public.bmp_ip_ribs (base_attr_hash_id);

create index bmp_ip_ribs_origin_as
    on public.bmp_ip_ribs (origin_as);

create table public.bmp_global_ip_ribs
(
    id             bigint                not null,
    prefix         varchar(40)           not null,
    should_delete  boolean default false not null,
    prefix_len     integer               not null,
    recv_origin_as bigint                not null,
    rpki_origin_as bigint,
    irr_origin_as  bigint,
    irr_source     varchar(32),
    last_updated   timestamp             not null,
    num_peers      integer default 0,
    constraint pk_bmp_global_ip_ribs
        primary key (prefix, recv_origin_as)
);

comment on table public.bmp_global_ip_ribs is 'bmp global ip ribs';

alter table public.bmp_global_ip_ribs
    owner to opennms;

create index bmp_global_ip_ribs_recv_origin_as
    on public.bmp_global_ip_ribs (recv_origin_as);

create index bmp_global_ip_ribs_should_delete
    on public.bmp_global_ip_ribs (should_delete);

create index bmp_global_ip_ribs_rpki_origin_as
    on public.bmp_global_ip_ribs (rpki_origin_as);

create index bmp_global_ip_ribs_irr_origin_as
    on public.bmp_global_ip_ribs (irr_origin_as);

create table public.bmp_asn_info
(
    id           bigint not null,
    asn          bigint not null
        constraint pk_bmp_asn_info
            primary key,
    as_name      varchar(255),
    org_id       varchar(255),
    org_name     varchar(255),
    remarks      text,
    address      text,
    city         varchar(255),
    state_prov   varchar(255),
    postal_code  varchar(255),
    country      varchar(255),
    raw_output   text,
    last_updated timestamp,
    source       varchar(64)
);

comment on table public.bmp_asn_info is 'bmp asn info';

alter table public.bmp_asn_info
    owner to opennms;

create table public.bmp_asn_path_analysis
(
    id                  bigint  not null,
    asn                 bigint  not null,
    asn_left            bigint  not null,
    asn_right           bigint  not null,
    asn_left_is_peering boolean not null,
    last_updated        timestamp,
    constraint pk_bmp_asn_path
        primary key (asn, asn_left, asn_right, asn_left_is_peering)
);

comment on table public.bmp_asn_path_analysis is 'bmp asn path analysis';

alter table public.bmp_asn_path_analysis
    owner to opennms;

create index bmp_asn_path_analysis_asn_left
    on public.bmp_asn_path_analysis (asn_left);

create index bmp_asn_path_analysis_asn_right
    on public.bmp_asn_path_analysis (asn_right);

create table public.bmp_route_info
(
    id           bigint      not null,
    prefix       varchar(40) not null,
    prefix_len   integer     not null,
    descr        text,
    origin_as    bigint      not null,
    source       varchar(32) not null,
    last_updated timestamp   not null,
    constraint pk_bmp_route_info
        primary key (prefix, prefix_len, origin_as)
);

comment on table public.bmp_route_info is 'bmp route info';

alter table public.bmp_route_info
    owner to opennms;

create index bmp_route_info_origin_as
    on public.bmp_route_info (origin_as);

create table public.bmp_ip_rib_log
(
    id                bigint                not null,
    base_attr_hash_id varchar(36)           not null,
    peer_hash_id      varchar(36)           not null,
    last_updated      timestamp             not null,
    prefix            varchar(40)           not null,
    prefix_len        integer               not null,
    origin_as         bigint                not null,
    is_withdrawn      boolean default false not null
);

comment on table public.bmp_ip_rib_log is 'bmp ip rib log';

alter table public.bmp_ip_rib_log
    owner to opennms;

create index bmp_ip_rib_log_peer_hash_id
    on public.bmp_ip_rib_log (peer_hash_id);

create index bmp_ip_rib_log_prefix
    on public.bmp_ip_rib_log (prefix);

create index bmp_ip_rib_log_origin_as
    on public.bmp_ip_rib_log (origin_as);

create table public.bmp_stats_by_peer
(
    id            bigint           not null,
    peer_hash_id  varchar(36)      not null,
    interval_time timestamp        not null,
    updates       bigint default 0 not null,
    withdraws     bigint default 0 not null
);

comment on table public.bmp_stats_by_peer is 'bmp stats by peer';

alter table public.bmp_stats_by_peer
    owner to opennms;

create index bmp_stats_by_peer_peer_hash_id
    on public.bmp_stats_by_peer (peer_hash_id);

create unique index bmp_stats_by_peer_peer_hash_id_interval_time
    on public.bmp_stats_by_peer (interval_time, peer_hash_id);

create table public.bmp_stats_by_asn
(
    id            bigint           not null,
    peer_hash_id  varchar(36)      not null,
    origin_as     bigint           not null,
    interval_time timestamp        not null,
    updates       bigint default 0 not null,
    withdraws     bigint default 0 not null
);

comment on table public.bmp_stats_by_asn is 'bmp stats by asn';

alter table public.bmp_stats_by_asn
    owner to opennms;

create index bmp_stats_by_asn_peer_hash_id
    on public.bmp_stats_by_asn (peer_hash_id);

create index bmp_stats_by_asn_origin_as
    on public.bmp_stats_by_asn (origin_as);

create unique index bmp_stats_by_asn_unique
    on public.bmp_stats_by_asn (interval_time, peer_hash_id, origin_as);

create table public.bmp_stats_by_prefix
(
    id            bigint           not null,
    peer_hash_id  varchar(36)      not null,
    prefix        varchar(40)      not null,
    prefix_len    integer          not null,
    interval_time timestamp        not null,
    updates       bigint default 0 not null,
    withdraws     bigint default 0 not null
);

comment on table public.bmp_stats_by_prefix is 'bmp stats by prefix';

alter table public.bmp_stats_by_prefix
    owner to opennms;

create index bmp_stats_by_prefix_peer_hash_id
    on public.bmp_stats_by_prefix (peer_hash_id);

create index bmp_stats_by_prefix_prefix
    on public.bmp_stats_by_prefix (prefix);

create unique index bmp_stats_by_prefix_unique
    on public.bmp_stats_by_prefix (interval_time, peer_hash_id, prefix);

create table public.bmp_stats_peer_rib
(
    id            bigint            not null,
    peer_hash_id  varchar(36)       not null,
    interval_time timestamp         not null,
    v4_prefixes   integer default 0 not null,
    v6_prefixes   integer default 0 not null
);

comment on table public.bmp_stats_peer_rib is 'bmp stats peer rib prefix counts';

alter table public.bmp_stats_peer_rib
    owner to opennms;

create index bmp_stats_peer_rib_peer_hash_id
    on public.bmp_stats_peer_rib (peer_hash_id);

create unique index bmp_stats_peer_rib_unique
    on public.bmp_stats_peer_rib (interval_time, peer_hash_id);

create table public.bmp_stats_ip_origins
(
    id            bigint            not null,
    asn           bigint            not null,
    interval_time timestamp         not null,
    v4_prefixes   integer default 0 not null,
    v6_prefixes   integer default 0 not null,
    v4_with_rpki  integer default 0 not null,
    v6_with_rpki  integer default 0 not null,
    v4_with_irr   integer default 0 not null,
    v6_with_irr   integer default 0 not null
);

comment on table public.bmp_stats_ip_origins is 'bmp stats ip origins';

alter table public.bmp_stats_ip_origins
    owner to opennms;

create unique index bmp_stats_ip_origins_unique
    on public.bmp_stats_ip_origins (interval_time, asn);

create table public.bmp_rpki_info
(
    id             bigint            not null,
    prefix         varchar(40)       not null,
    prefix_len     integer default 0 not null,
    prefix_len_max integer default 0 not null,
    origin_as      bigint            not null,
    last_updated   timestamp         not null,
    constraint pk_bmp_rpki_info
        primary key (prefix, prefix_len_max, origin_as)
);

comment on table public.bmp_rpki_info is 'bmp rpki info';

alter table public.bmp_rpki_info
    owner to opennms;

create index bmp_rpki_info_origin_as
    on public.bmp_rpki_info (origin_as);

create table public.device_config
(
    id             bigint                                        not null
        primary key,
    ipinterface_id integer                                       not null,
    service_name   varchar(64),
    config         bytea,
    encoding       varchar(16),
    created_time   timestamp,
    config_type    varchar(64),
    filename       varchar(1024),
    failure_reason varchar(1024),
    last_succeeded timestamp,
    last_failed    timestamp,
    last_updated   timestamp,
    status         varchar(20) default 'NONE'::character varying not null
);

alter table public.device_config
    owner to opennms;

create table public.usage_analytics
(
    id          bigserial
        primary key,
    metric_name text   not null
        unique,
    namespace   text,
    counter     bigint not null
);

alter table public.usage_analytics
    owner to opennms;

create table public.ospfarea
(
    id                   bigint default nextval('opennmsnxtid'::regclass) not null
        primary key,
    nodeid               integer                                          not null
        constraint fk_ospfarea_nodeid
            references public.node
            on delete cascade,
    ospfareaid           varchar(16)                                      not null,
    ospfauthtype         integer,
    ospfimportasextern   integer,
    ospfareabdrrtrcount  integer,
    ospfasbdrrtrcount    integer,
    ospfarealsacount     integer,
    ospfareacreatetime   timestamp with time zone                         not null,
    ospfarealastpolltime timestamp with time zone                         not null,
    constraint ospfarea_pk_idx
        unique (nodeid, ospfareaid)
);

alter table public.ospfarea
    owner to opennms;

create index ospfarea_nodeid_idx
    on public.ospfarea (nodeid);

create view public.node_alarm_status
            (nodeid, max_alarm_severity, max_alarm_severity_unack, alarm_count_unack, alarm_count) as
SELECT nodeid,
       COALESCE((SELECT max(
                                CASE
                                    WHEN alarms.severity IS NULL OR alarms.severity < 3 THEN 3
                                    ELSE alarms.severity
                                    END) AS max
                 FROM alarms
                 WHERE alarms.nodeid = node.nodeid), 3) AS max_alarm_severity,
       COALESCE((SELECT max(
                                CASE
                                    WHEN alarms.severity IS NULL OR alarms.severity < 3 THEN 3
                                    ELSE alarms.severity
                                    END) AS max
                 FROM alarms
                 WHERE alarms.nodeid = node.nodeid
                   AND alarms.alarmacktime IS NULL), 3) AS max_alarm_severity_unack,
       (SELECT count(alarms.alarmid) AS count
        FROM alarms
        WHERE alarms.nodeid = node.nodeid
          AND alarms.alarmacktime IS NULL)              AS alarm_count_unack,
       (SELECT count(*) AS count
        FROM alarms
        WHERE alarms.nodeid = node.nodeid)              AS alarm_count
FROM node;

alter table public.node_alarm_status
    owner to opennms;

create view public.node_alarms
            (nodeid, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription,
             nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname,
             operatingsystem, lastcapsdpoll, foreignsource, foreignid, location, alarmid, eventuei, ipaddr,
             reductionkey, alarmtype, counter, severity, lasteventid, firsteventtime, lasteventtime,
             firstautomationtime, lastautomationtime, description, logmsg, operinstruct, tticketid, tticketstate,
             suppresseduntil, suppresseduser, suppressedtime, alarmackuser, alarmacktime, managedobjectinstance,
             managedobjecttype, applicationdn, ossprimarykey, x733alarmtype, qosalarmstate, clearkey, ifindex,
             stickymemo, systemid, acknowledged, categoryname, categorydescription, servicename, max_alarm_severity,
             max_alarm_severity_unack, alarm_count_unack, alarm_count)
as
SELECT n.nodeid,
       n.nodecreatetime,
       n.nodeparentid,
       n.nodetype,
       n.nodesysoid,
       n.nodesysname,
       n.nodesysdescription,
       n.nodesyslocation,
       n.nodesyscontact,
       n.nodelabel,
       n.nodelabelsource,
       n.nodenetbiosname,
       n.nodedomainname,
       n.operatingsystem,
       n.lastcapsdpoll,
       n.foreignsource,
       n.foreignid,
       n.location,
       a.alarmid,
       a.eventuei,
       a.ipaddr,
       a.reductionkey,
       a.alarmtype,
       a.counter,
       a.severity,
       a.lasteventid,
       a.firsteventtime,
       a.lasteventtime,
       a.firstautomationtime,
       a.lastautomationtime,
       a.description,
       a.logmsg,
       a.operinstruct,
       a.tticketid,
       a.tticketstate,
       a.suppresseduntil,
       a.suppresseduser,
       a.suppressedtime,
       a.alarmackuser,
       a.alarmacktime,
       a.managedobjectinstance,
       a.managedobjecttype,
       a.applicationdn,
       a.ossprimarykey,
       a.x733alarmtype,
       a.qosalarmstate,
       a.clearkey,
       a.ifindex,
       a.stickymemo,
       a.systemid,
       a.alarmacktime IS NOT NULL                        AS acknowledged,
       COALESCE(s_cat.categoryname, 'no category'::text) AS categoryname,
       s_cat.categorydescription,
       s.servicename,
       nas.max_alarm_severity,
       nas.max_alarm_severity_unack,
       nas.alarm_count_unack,
       nas.alarm_count
FROM node n
         JOIN alarms a ON n.nodeid = a.nodeid
         JOIN node_alarm_status nas ON a.nodeid = nas.nodeid
         LEFT JOIN service s ON a.serviceid = s.serviceid
         LEFT JOIN category_node cat ON n.nodeid = cat.nodeid
         LEFT JOIN categories s_cat ON cat.categoryid = s_cat.categoryid;

alter table public.node_alarms
    owner to opennms;

create view public.node_categories
            (nodeid, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription,
             nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname,
             operatingsystem, lastcapsdpoll, foreignsource, foreignid, location, last_ingress_flow, last_egress_flow,
             categoryname)
as
SELECT n.nodeid,
       n.nodecreatetime,
       n.nodeparentid,
       n.nodetype,
       n.nodesysoid,
       n.nodesysname,
       n.nodesysdescription,
       n.nodesyslocation,
       n.nodesyscontact,
       n.nodelabel,
       n.nodelabelsource,
       n.nodenetbiosname,
       n.nodedomainname,
       n.operatingsystem,
       n.lastcapsdpoll,
       n.foreignsource,
       n.foreignid,
       n.location,
       n.last_ingress_flow,
       n.last_egress_flow,
       COALESCE(s_cat.categoryname, 'no category'::text) AS categoryname
FROM node n
         LEFT JOIN category_node cn ON n.nodeid = cn.nodeid
         LEFT JOIN categories s_cat ON cn.categoryid = s_cat.categoryid;

alter table public.node_categories
    owner to opennms;

create view public.node_ip_services
            (nodeid, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription,
             nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname,
             operatingsystem, lastcapsdpoll, foreignsource, foreignid, location, last_ingress_flow, last_egress_flow,
             categoryname, ip_if_id, ipaddr, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary,
             snmpinterfaceid, netmask, serviceid, servicename, if_svc_id, if_svc_ifindex, if_svc_status)
as
SELECT n.nodeid,
       n.nodecreatetime,
       n.nodeparentid,
       n.nodetype,
       n.nodesysoid,
       n.nodesysname,
       n.nodesysdescription,
       n.nodesyslocation,
       n.nodesyscontact,
       n.nodelabel,
       n.nodelabelsource,
       n.nodenetbiosname,
       n.nodedomainname,
       n.operatingsystem,
       n.lastcapsdpoll,
       n.foreignsource,
       n.foreignid,
       n.location,
       n.last_ingress_flow,
       n.last_egress_flow,
       n.categoryname,
       ip_if.id       AS ip_if_id,
       ip_if.ipaddr,
       ip_if.iphostname,
       ip_if.ismanaged,
       ip_if.ipstatus,
       ip_if.iplastcapsdpoll,
       ip_if.issnmpprimary,
       ip_if.snmpinterfaceid,
       ip_if.netmask,
       svc.serviceid,
       svc.servicename,
       if_svc.id      AS if_svc_id,
       if_svc.ifindex AS if_svc_ifindex,
       if_svc.status  AS if_svc_status
FROM node_categories n
         LEFT JOIN ipinterface ip_if ON ip_if.nodeid = n.nodeid
         LEFT JOIN ifservices if_svc ON ip_if.id = if_svc.ipinterfaceid
         LEFT JOIN service svc ON if_svc.serviceid = svc.serviceid;

alter table public.node_ip_services
    owner to opennms;

create view public.node_outage_status(nodeid, max_outage_severity) as
SELECT node.nodeid,
       CASE
           WHEN tmp.severity IS NULL OR tmp.severity < 3 THEN 3
           ELSE tmp.severity
           END AS max_outage_severity
FROM (SELECT events.nodeid,
             max(events.eventseverity) AS severity
      FROM events
               JOIN outages ON outages.svclosteventid = events.eventid
      WHERE outages.ifregainedservice IS NULL
        AND outages.perspective IS NULL
      GROUP BY events.nodeid) tmp
         RIGHT JOIN node ON tmp.nodeid = node.nodeid;

alter table public.node_outage_status
    owner to opennms;

create view public.node_outages
            (outageid, svclosteventid, svcregainedeventid, iflostservice, ifregainedservice, ifserviceid,
             svclosteventuei, eventsource, alarmid, eventseverity, resolved, servicename, serviceid, ipaddr, duration,
             max_outage_severity, nodeid, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname,
             nodesysdescription, nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname,
             nodedomainname, operatingsystem, lastcapsdpoll, foreignsource, foreignid, location, last_ingress_flow,
             last_egress_flow, categoryname)
as
SELECT outages.outageid,
       outages.svclosteventid,
       outages.svcregainedeventid,
       outages.iflostservice,
       outages.ifregainedservice,
       outages.ifserviceid,
       e.eventuei                                                                                 AS svclosteventuei,
       e.eventsource,
       e.alarmid,
       e.eventseverity,
       outages.ifregainedservice IS NOT NULL                                                      AS resolved,
       s.servicename,
       i.serviceid,
       ipif.ipaddr,
       COALESCE(outages.ifregainedservice - outages.iflostservice, now() - outages.iflostservice) AS duration,
       nos.max_outage_severity,
       nc.nodeid,
       nc.nodecreatetime,
       nc.nodeparentid,
       nc.nodetype,
       nc.nodesysoid,
       nc.nodesysname,
       nc.nodesysdescription,
       nc.nodesyslocation,
       nc.nodesyscontact,
       nc.nodelabel,
       nc.nodelabelsource,
       nc.nodenetbiosname,
       nc.nodedomainname,
       nc.operatingsystem,
       nc.lastcapsdpoll,
       nc.foreignsource,
       nc.foreignid,
       nc.location,
       nc.last_ingress_flow,
       nc.last_egress_flow,
       nc.categoryname
FROM outages
         JOIN events e ON outages.svclosteventid = e.eventid
         JOIN ifservices i ON outages.ifserviceid = i.id
         JOIN service s ON i.serviceid = s.serviceid
         JOIN ipinterface ipif ON i.ipinterfaceid = ipif.id
         JOIN node_categories nc ON nc.nodeid = e.nodeid
         JOIN node_outage_status nos ON nc.nodeid = nos.nodeid
WHERE outages.perspective IS NULL;

alter table public.node_outages
    owner to opennms;

create view public.bmp_v_peers
            (routername, routerip, localip, localport, localasn, localbgpid, peername, peerip, peerport, peerasn,
             peerbgpid, localholdtime, peerholdtime, peer_state, router_state, ispeeripv4, ispeervpn, isprepolicy,
             lastmodified, lastbmpreasoncode, lastdowncode, lastdownsubcode, lastdownmessage, lastdowntimestamp,
             sentcapabilities, recvcapabilities, as_name, is_loc_rib, is_loc_rib_filtered, table_name, peer_hash_id,
             router_hash_id, geo_ip_start)
as
SELECT CASE
           WHEN length(rtr.name::text) > 0 THEN rtr.name
           ELSE rtr.ip_address
           END             AS routername,
       rtr.ip_address      AS routerip,
       p.local_ip          AS localip,
       p.local_port        AS localport,
       p.local_asn         AS localasn,
       p.local_bgp_id      AS localbgpid,
       CASE
           WHEN length(p.name::text) > 0 THEN p.name
           ELSE p.peer_addr
           END             AS peername,
       p.peer_addr         AS peerip,
       p.remote_port       AS peerport,
       p.peer_asn          AS peerasn,
       p.peer_bgp_id       AS peerbgpid,
       p.local_hold_time   AS localholdtime,
       p.remote_hold_time  AS peerholdtime,
       p.state             AS peer_state,
       rtr.state           AS router_state,
       p.is_ipv4           AS ispeeripv4,
       p.is_l3vpn_peer     AS ispeervpn,
       p.is_pre_policy     AS isprepolicy,
       p.last_updated      AS lastmodified,
       p.bmp_reason        AS lastbmpreasoncode,
       p.bgp_err_code      AS lastdowncode,
       p.bgp_err_subcode   AS lastdownsubcode,
       p.error_text        AS lastdownmessage,
       p.last_updated      AS lastdowntimestamp,
       p.sent_capabilities AS sentcapabilities,
       p.recv_capabilities AS recvcapabilities,
       w.as_name,
       p.is_loc_rib,
       p.is_loc_rib_filtered,
       p.table_name,
       p.hash_id           AS peer_hash_id,
       rtr.hash_id         AS router_hash_id,
       p.geo_ip_start
FROM bmp_peers p
         JOIN bmp_routers rtr ON p.router_hash_id::text = rtr.hash_id::text
         LEFT JOIN bmp_asn_info w ON p.peer_asn = w.asn;

alter table public.bmp_v_peers
    owner to opennms;

create view public.bmp_v_ip_routes
            (routername, peername, prefix, prefixlen, origin, origin_as, med, localpref, nh, as_path, aspath_count,
             communities, extcommunities, largecommunities, clusterlist, aggregator, peeraddress, peerasn, isipv4,
             ispeeripv4, ispeervpn, lastmodified, firstaddedtimestamp, path_id, labels, rib_hash_id, base_hash_id,
             peer_hash_id, router_hash_id, is_withdrawn, prefix_bits, is_pre_policy, is_adj_ribin)
as
SELECT CASE
           WHEN length(rtr.name::text) > 0 THEN rtr.name
           ELSE rtr.ip_address
           END                   AS routername,
       CASE
           WHEN length(p.name::text) > 0 THEN p.name
           ELSE p.peer_addr
           END                   AS peername,
       r.prefix,
       r.prefix_len              AS prefixlen,
       attr.origin,
       r.origin_as,
       attr.med,
       attr.local_pref           AS localpref,
       attr.next_hop             AS nh,
       attr.as_path,
       attr.as_path_count        AS aspath_count,
       attr.community_list       AS communities,
       attr.ext_community_list   AS extcommunities,
       attr.large_community_list AS largecommunities,
       attr.cluster_list         AS clusterlist,
       attr.aggregator,
       p.peer_addr               AS peeraddress,
       p.peer_asn                AS peerasn,
       r.is_ipv4                 AS isipv4,
       p.is_ipv4                 AS ispeeripv4,
       p.is_l3vpn_peer           AS ispeervpn,
       r.last_updated            AS lastmodified,
       r.first_added_timestamp   AS firstaddedtimestamp,
       r.path_id,
       r.labels,
       r.hash_id                 AS rib_hash_id,
       r.base_attr_hash_id       AS base_hash_id,
       r.peer_hash_id,
       rtr.hash_id               AS router_hash_id,
       r.is_withdrawn,
       r.prefix_bits,
       r.is_pre_policy,
       r.is_adj_ribin
FROM bmp_ip_ribs r
         JOIN bmp_peers p ON r.peer_hash_id::text = p.hash_id::text
         JOIN bmp_base_attributes attr
              ON attr.hash_id::text = r.base_attr_hash_id::text AND attr.peer_hash_id::text = r.peer_hash_id::text
         JOIN bmp_routers rtr ON p.router_hash_id::text = rtr.hash_id::text;

alter table public.bmp_v_ip_routes
    owner to opennms;

create view public.bmp_v_ip_routes_history
            (routername, routeraddress, peername, prefix, prefixlen, origin, origin_as, med, localpref, nh, as_path,
             aspath_count, communities, extcommunities, largecommunities, clusterlist, aggregator, peerip, peerasn,
             ispeeripv4, ispeervpn, id, lastmodified, event, base_attr_hash_id, peer_hash_id, router_hash_id)
as
SELECT CASE
           WHEN length(rtr.name::text) > 0 THEN rtr.name
           ELSE rtr.ip_address
           END                   AS routername,
       rtr.ip_address            AS routeraddress,
       CASE
           WHEN length(p.name::text) > 0 THEN p.name
           ELSE p.peer_addr
           END                   AS peername,
       log.prefix,
       log.prefix_len            AS prefixlen,
       attr.origin,
       log.origin_as,
       attr.med,
       attr.local_pref           AS localpref,
       attr.next_hop             AS nh,
       attr.as_path,
       attr.as_path_count        AS aspath_count,
       attr.community_list       AS communities,
       attr.ext_community_list   AS extcommunities,
       attr.large_community_list AS largecommunities,
       attr.cluster_list         AS clusterlist,
       attr.aggregator,
       p.peer_addr               AS peerip,
       p.peer_asn                AS peerasn,
       p.is_ipv4                 AS ispeeripv4,
       p.is_l3vpn_peer           AS ispeervpn,
       log.id,
       log.last_updated          AS lastmodified,
       CASE
           WHEN log.is_withdrawn THEN 'Withdrawn'::text
           ELSE 'Advertised'::text
           END                   AS event,
       log.base_attr_hash_id,
       log.peer_hash_id,
       rtr.hash_id               AS router_hash_id
FROM bmp_ip_rib_log log
         JOIN bmp_base_attributes attr
              ON log.base_attr_hash_id::text = attr.hash_id::text AND log.peer_hash_id::text = attr.peer_hash_id::text
         JOIN bmp_peers p ON log.peer_hash_id::text = p.hash_id::text
         JOIN bmp_routers rtr ON p.router_hash_id::text = rtr.hash_id::text;

alter table public.bmp_v_ip_routes_history
    owner to opennms;
