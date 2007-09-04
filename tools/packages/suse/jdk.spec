# keep RPM from making an empty debug package
%define debug_package %{nil}
%define package_version 1.5.0
%define package_version_translated %(echo %{package_version} | sed -e 's,\\\.,_,g')
%define package_epoch 2000

Name:			jdk
Summary:		Placeholder package for JPackage-style JDK
Release:		1
Version:		%{package_version}
Epoch:			%{package_epoch}
License:		Public Domain
Group:			Development/Tools
BuildArch:		noarch

AutoReqProv:		no

Requires:		java-%{package_version_translated}-sun-devel

%description
This is a placeholder wrapper package to provide a "jdk" dependency but to
use the JPackage/SuSE-style "java-X_X_X-sun-devel" style package under the
covers.

%files
