create type public.daily_series as
    (
    ds  timestamp,
    de  timestamp,
    dow integer
    );

alter type public.daily_series owner to opennms;