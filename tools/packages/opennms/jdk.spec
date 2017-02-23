# keep RPM from making an empty debug package
%define debug_package %{nil}

%{!?package_arch:%define package_arch noarch}
%{!?package_version:%define package_version 1.8}
%{!?package_release:%define package_release 1}
%{!?package_epoch:%define package_epoch 2000}
%{!?my_epoch:%define my_epoch %{package_epoch}}
%{!?my_release:%define my_release %{package_release}}
%{!?dep_package:%define dep_package java-%(echo %{package_version} | sed -e 's,\\\.,_,g')-sun-devel}

Name:			jdk
Summary:		Sun JDK compatible placeholder
Epoch:			%{my_epoch}
Version:		%{package_version}
Release:		%{my_release}
License:		Public Domain
Group:			Development/Tools
BuildArch:		%{package_arch}

AutoReqProv:		no

Requires:		%{dep_package} >= %{package_epoch}:%{package_version}-%{package_release}

%description
This is a placeholder wrapper package to provide a "jdk" dependency but to
use the OS-provided JDK instead.

%files
