# keep RPM from making an empty debug package
%define debug_package %{nil}

%{!?package_version:%define version 1.3.10}

%{!?package_version:%define package_version 1.5.0}
%{!?package_epoch:%define package_epoch 2000}
%{!?dep_package:%define dep_package java-%(echo %{package_version} | sed -e 's,\\\.,_,g')-sun-devel}

Name:			jdk
Summary:		Sun JDK compatible placeholder
Release:		1
Version:		%{package_version}
Epoch:			%{package_epoch}
License:		Public Domain
Group:			Development/Tools
BuildArch:		noarch

AutoReqProv:		no

Requires:		%{dep_package} >= %{package_epoch}:%{package_version}

%description
This is a placeholder wrapper package to provide a "jdk" dependency but to
use the OS-provided JDK instead.

%files
