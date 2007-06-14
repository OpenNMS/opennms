Name: iplike
Version: 1.0
Release: 1
License: GPL
Group: Applications/Databases
Summary: PostgreSQL complex IP Address text field query
Source: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-root

BuildRequires: postgresql-devel

%description
PostgreSQL function for doing complex IP address queries
on a text field.

%prep
%setup

%build
%configure
make

%install
%makeinstall

%files
%{_prefix}/lib/iplike*

%changelog
* Wed Jun 13 2007 Benjamin Reed <ranger@opennms.org> 1.0-1
- initial package
