# keep RPM from making an empty debug package
%define debug_package %{nil}

Name:			debian-shunt
Summary:		Placeholder package to make RPM on debian happy
Version:		1.0
Release:		3
License:		Public Domain
Group:			Development/Tools
BuildArch:		noarch

AutoReqProv:		no

Provides: /bin/sh
Provides: /bin/bash
Provides: jrrd = 1.0.5
Provides: jicmp = 1.4.1
Provides: jicmp6 = 1.2.1
Provides: postgresql-server = 9.3
Provides: jdk = 2000:1.8.0
Provides: java-1.8.0
Provides: jre-1.8.0
Provides: java-11
Provides: java-11-openjdk
Provides: jre-11
Provides: jre-11-openjdk
Provides: java-11-devel
Provides: java-11-openjdk-devel
Provides: java-sdk-11
Provides: java-sdk-11-openjdk

%description
This is a placeholder wrapper package to provide the dependencies necessary
to make installing OpenNMS on our Ubuntu-based bamboo systems possible.

%files
