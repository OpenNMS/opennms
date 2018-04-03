#----------------------
# Include modern UI 2.0, sections, and nsDialogs plugins
#----------------------
!include "MUI2.nsh"
!include "Sections.nsh"
!include "nsDialogs.nsh"
!include "WinVer.nsh"

# If we're building inside Maven, this include file will be present
!include /NONFATAL "target\project.nsh"

# Define any variables if they were not defined by Maven
!ifndef PROJECT_VERSION
  !define PROJECT_VERSION ""
!endif
!ifndef PROJECT_NAME
  !define PROJECT_NAME "OpenNMS Remote Poller"
!endif

# Configure the interface
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "resources\opennms-nsis-brand.bmp"
!define MUI_ABORTWARNING

# Include WinMessages.nsh so that we can send messages
# using symbolic names
!include "WinMessages.nsh"

!define POLICY_LOOKUP_NAMES 0x00000800
!define POLICY_LOOKUP_NAMES_CREATE_ACCOUNT 0x00000810
!define strLSA_OBJECT_ATTRIBUTES '(i,i,w,i,i,i)i'
!define strLSA_UNICODE_STRING '(&i2,&i2,w)i'


#----------------------
# Declare some vars that we will use in .onInit
#----------------------
Var ServiceUser
Var ServicePassword
Var ServiceDomain
Var ComputerName

Var JavaHome

Var OnmsWebappProtocol
Var OnmsWebappServer
Var OnmsWebappPort
Var OnmsWebappPath
Var OnmsWebappUsername
Var OnmsWebappPassword

Var POLLER_SERVICE_FILE_NAME
Var POLLER_TRAY_FILE_NAME
Var UNINSTALLER_FILE_NAME
Var GUI_POLLER_JNLP
Var HEADLESS_POLLER_JNLP
Var POLLER_SVC_NAME
Var GUI_POLLER_SVC_NAME
Var POLLER_SVC_DISP_NAME
Var POLLER_SVC_DESCRIPTION
Var POLLER_PROPS_FILE

Var DEFAULT_WEBUI_PROTOCOL
Var DEFAULT_WEBUI_HOST
Var DEFAULT_WEBUI_PORT
Var DEFAULT_WEBUI_PATH
Var DEFAULT_WEBUI_USERNAME
Var DEFAULT_WEBUI_PASSWORD

Var KILL_SWITCH_FILE_NAME
Var VBS_KILL_SCRIPT

Var INSTDIRJAVA
Var PROFILEJAVA
Var ProcrunJWSOptions


#----------------------
# We cannot use defines in .onInit, so put it at the top for easy access
#----------------------
Function .onInit
  # First, prevent multiple instances of this installer.
  System::Call 'kernel32::CreateMutexA(i 0, i 0, t "onmsRpMutex") i .r1 ?e'
  Pop $R0

  StrCmp $R0 0 +3
    MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is already running."
    Abort

  ReadEnvStr $ServiceUser "USERNAME"
  ReadEnvStr $ServiceDomain "USERDOMAIN"
  ReadEnvStr $ComputerName "COMPUTERNAME"

  ${IfNot} ${AtLeastWinVista}
    MessageBox MB_OK|MB_ICONEXCLAMATION "The OpenNMS Remote Poller can only be installed on Windows Vista or higher."
    Abort
  ${EndIf}

  ClearErrors
  UserInfo::GetName
  Pop $0
  UserInfo::GetAccountType
  Pop $1
  StrCmp $1 "Admin" IsAdmin
    MessageBox MB_OK|MB_ICONEXCLAMATION "This installer must be run with administrative privileges."
    Abort
  IsAdmin:
  StrCmp $ServiceDomain $ComputerName UserLocal
    Abort "This installer must be run as a local user."
  UserLocal:

  # Count the number of javaws.exe locations
  # 0 on the stack means push only the number
  Push 0
  Call GetJavaHomeCandidates
  Pop $1
  StrCmp $1 "0" 0 GotJava
    MessageBox MB_OK|MB_ICONEXCLAMATION "A Java 8 runtime environment or development kit with support$\r$\nfor Java Web Start is required, but none was found on this system.$\r$\n$\r$\nPlease download and install an appropriate Java distribution$\r$\nfrom http://java.sun.com/ and run the installer again."
    Abort
  GotJava:

  StrCpy $POLLER_SERVICE_FILE_NAME "poller.exe"
  StrCpy $POLLER_TRAY_FILE_NAME "polltray.exe"

  StrCpy $UNINSTALLER_FILE_NAME "uninstall-remote-poller.exe"
  StrCpy $GUI_POLLER_JNLP "app.jnlp"
  StrCpy $HEADLESS_POLLER_JNLP "headless.jnlp"
  StrCpy $POLLER_SVC_NAME "OpenNMSRemotePoller"
  StrCpy $GUI_POLLER_SVC_NAME "OpenNMSRemotePollerGUI"
  StrCpy $POLLER_SVC_DISP_NAME "${PROJECT_NAME}"
  StrCpy $POLLER_SVC_DESCRIPTION "Measures uptime and latency of services from remote locations, sending the data back to an OpenNMS server"
  StrCpy $POLLER_PROPS_FILE "$PROFILE\.opennms\remote-poller.properties"
  StrCpy $KILL_SWITCH_FILE_NAME "remote-poller.run"
  StrCpy $VBS_KILL_SCRIPT "pollkill.vbs"

  StrCpy $DEFAULT_WEBUI_PROTOCOL "http"
  StrCpy $DEFAULT_WEBUI_HOST ""
  StrCpy $DEFAULT_WEBUI_PORT "8980"
  StrCpy $DEFAULT_WEBUI_PATH "/opennms"
  StrCpy $DEFAULT_WEBUI_USERNAME "admin"
  StrCpy $DEFAULT_WEBUI_PASSWORD "admin"

  StrCpy $OnmsWebappProtocol "$DEFAULT_WEBUI_PROTOCOL"
  StrCpy $OnmsWebappServer "$DEFAULT_WEBUI_HOST"
  StrCpy $OnmsWebappPort "$DEFAULT_WEBUI_PORT"
  StrCpy $OnmsWebappPath "$DEFAULT_WEBUI_PATH"
  StrCpy $OnmsWebappUsername "$DEFAULT_WEBUI_USERNAME"
  StrCpy $OnmsWebappPassword "$DEFAULT_WEBUI_PASSWORD"

FunctionEnd

#----------------------
# Uninstall init method
#----------------------
Function un.onInit
  # First, prevent multiple instances of this installer.
  System::Call 'kernel32::CreateMutexA(i 0, i 0, t "onmsRpMutex") i .r1 ?e'
  Pop $R0

  StrCmp $R0 0 +3
    MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is already running."
    Abort

  UserInfo::GetAccountType
  Pop $1
  StrCmp $1 "Admin" IsAdmin
    MessageBox MB_OK|MB_ICONEXCLAMATION "This installer must be run with administrative privileges."
    Abort
  IsAdmin:

  ReadRegStr $ServiceUser HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "ServiceUser"
  ReadRegStr $ServiceDomain HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "ServiceDomain"

  StrCpy $POLLER_SERVICE_FILE_NAME "poller.exe"
  StrCpy $POLLER_TRAY_FILE_NAME "polltray.exe"

  StrCpy $UNINSTALLER_FILE_NAME "uninstall-remote-poller.exe"
  StrCpy $GUI_POLLER_JNLP "app.jnlp"
  StrCpy $HEADLESS_POLLER_JNLP "headless.jnlp"
  StrCpy $POLLER_SVC_NAME "OpenNMSRemotePoller"
  StrCpy $GUI_POLLER_SVC_NAME "OpenNMSRemotePollerGUI"
  StrCpy $POLLER_SVC_DISP_NAME "${PROJECT_NAME}"
  StrCpy $POLLER_SVC_DESCRIPTION "Measures uptime and latency of services from remote locations, sending the data back to an OpenNMS server"
  ReadRegStr $POLLER_PROPS_FILE HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "PollerPropsFile"
  StrCpy $KILL_SWITCH_FILE_NAME "remote-poller.run"
  StrCpy $VBS_KILL_SCRIPT "pollkill.vbs"

  StrCpy $DEFAULT_WEBUI_PROTOCOL "http"
  StrCpy $DEFAULT_WEBUI_HOST ""
  StrCpy $DEFAULT_WEBUI_PORT "8980"
  StrCpy $DEFAULT_WEBUI_PATH "/opennms"
  StrCpy $DEFAULT_WEBUI_USERNAME "admin"
  StrCpy $DEFAULT_WEBUI_PASSWORD "admin"

FunctionEnd


#----------------------
# Pages
#----------------------
!insertmacro MUI_PAGE_WELCOME

!insertmacro MUI_PAGE_LICENSE "resources\agpl-3.0.txt"

Page custom javaCheckPage javaCheckPageLeave

!insertmacro MUI_PAGE_DIRECTORY

!insertmacro MUI_PAGE_COMPONENTS

PageEx instfiles
  CompletedText "Click Next to configure the ${PROJECT_NAME} on your system."
PageExEnd

Page custom removeOldRegFilePage removeOldRegFilePageLeave

Page custom onmsServerInfoPage onmsServerInfoPageLeave

Page custom onmsSvcUserPage onmsSvcUserPageLeave

Page custom launchGuiPollerPage launchGuiPollerPageLeave

Page custom svcCreationPage svcCreationPageLeave

Page custom svcStartPage

!insertmacro MUI_PAGE_FINISH

#Page custom allDonePage


#----------------------
# Uninstaller pages
#----------------------
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
UninstPage custom un.OptionsPage un.OptionsPageLeave
UninstPage instfiles


#----------------------
# Language
#----------------------
!insertmacro MUI_LANGUAGE "English"


#----------------------
# Basic attributes of this installer
#----------------------
Name "${PROJECT_NAME}"
Icon resources\big-o-install.ico
UninstallIcon resources\big-o-uninstall.ico
# If this is a Maven build, leave OutFile undefined
!ifndef PROJECT_GROUP_ID
  OutFile opennms-remote-poller.exe
!endif

# File attributes for the installer EXE
VIProductVersion                 "1.0.0.0"
VIAddVersionKey FileDescription  "${PROJECT_NAME} Installer"
VIAddVersionKey FileVersion      1
VIAddVersionKey ProductName      "${PROJECT_NAME}"
VIAddVersionKey ProductVersion   "${PROJECT_VERSION}"
VIAddVersionKey LegalCopyright   "Copyright 2008-2018 The OpenNMS Group, Inc."
VIAddVersionKey Comments         ""
VIAddVersionKey CompanyName      "The OpenNMS Group, Inc."

# Where we want to be installed
InstallDir "$PROGRAMFILES\${PROJECT_NAME}"

# If set in a previous install, use that instead of InstallDir
InstallDirRegKey HKLM "SOFTWARE\The OpenNMS Group\${PROJECT_NAME}" InstallLocation

# On Vista and later, request administrator privilege
RequestExecutionLevel admin

# Include an XP manifest
XPStyle On

BrandingText "Copyright 2008-2018 The OpenNMS Group, Inc.  Installer made with NSIS."

#AddBrandingImage top 110

LicenseData resources\agpl-3.0.txt
#LicenseForceSelection checkbox


#----------------------
# Variables used in our custom dialogs
#----------------------
Var Dialog
Var ServiceUserLabel
Var ServiceUserText
Var ServicePasswordLabel
Var ServicePasswordText
Var ServicePasswordRepeatLabel
Var ServicePasswordRepeatText

Var TopLabel
Var ServerLabel
Var ServerText
Var PortLabel
Var PortText
Var AppPathLabel
Var AppPathText
Var UsernameLabel
Var UsernameText
Var PasswordLabel
Var PasswordText
Var HttpsCheckbox
Var DebugCheckbox

Var LaunchGuiLabel
Var JWSEXE
Var JnlpDebugOptions
Var ProcrunDebugOptions
Var JnlpUrl

Var WantSystray

Var JavaListBox

Var ShouldRemovePollerProps

#----------------------
# A few temporary variables
#----------------------
Var TEMP0
Var TEMP1
Var TEMP2
Var TEMP3
Var TEMP4


#----------------------
# Sections here
#----------------------

Section "-Files"
  SetOutPath $INSTDIR
  Push $INSTDIR
  Call MkJavaPath
  Pop $INSTDIRJAVA
  Push $PROFILE
  Call MkJavaPath
  Pop $PROFILEJAVA

  File resources\agpl-3.0.txt

  CreateDirectory "$INSTDIR\bin"
  CreateDirectory "$INSTDIR\etc"
  CreateDirectory "$INSTDIR\logs"

  DetailPrint "Writing logging configuration..."
  Sleep 200
  Call WriteCustomLogPropsFile

  DetailPrint "Writing service stop script..."
  Sleep 200
  Call WriteCustomVbsKillScript

  DetailPrint "Shutting down the $POLLER_SVC_DISP_NAME service and system tray monitor..."
  Sleep 200

  StrCpy $TEMP0 ""

  # Warn the user that we are stopping the service
  IfFileExists "$PROFILE\.opennms\$KILL_SWITCH_FILE_NAME" 0 DoneServiceStopNotify
    StrCpy $TEMP0 "Yes"
    Delete "$PROFILE\.opennms\$KILL_SWITCH_FILE_NAME"
    MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is stopping the $POLLER_SVC_DISP_NAME service.$\r$\n$\r$\nIf you abort the installer, you will need to restart the service manually."
  DoneServiceStopNotify:

  IfFileExists "$INSTDIR\bin\$POLLER_TRAY_FILE_NAME" 0 DoneStopSystray
    Call StopSystrayMonitor
  DoneStopSystray:

  StrCmp $TEMP0 "Yes" 0 DoneSleep
    DetailPrint "Shutting down the $POLLER_SVC_DISP_NAME service and system tray monitor..."
    # Break up this sleep so that the progress bar advances
    Sleep 3000
    Sleep 3000
    Sleep 3000
    Sleep 3000
    Sleep 3000
    Sleep 3000
    Sleep 3000
    Sleep 3000
    Sleep 3000
    Sleep 3000
  DoneSleep:

  # Rename the commons-daemon executables to nicer names
  DetailPrint "Installing $POLLER_SVC_DISP_NAME service and system tray monitor executables..."
  Sleep 200
  SetOutPath $INSTDIR\bin
  File /oname=$POLLER_SERVICE_FILE_NAME bin\prunsrv.exe
  File /oname=$POLLER_TRAY_FILE_NAME bin\prunmgr.exe

  DetailPrint "Updating service registry entries..."
  Sleep 200
  WriteRegStr HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "InstallLocation" "$INSTDIR"
  WriteRegStr HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "PollerPropsFile" "$POLLER_PROPS_FILE"
  WriteRegStr HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "ServiceUser" "$ServiceUser"
  WriteRegStr HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "ServiceDomain" "$ServiceDomain"

  DetailPrint "Writing uninstaller..."
  Sleep 200
  WriteUninstaller "$INSTDIR\$UNINSTALLER_FILE_NAME"
  Call WriteAddRemProgEntry
SectionEnd

Section "System Tray Icon" SecSystray
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Run" "$POLLER_SVC_NAME" '"$INSTDIR\bin\$POLLER_TRAY_FILE_NAME" //MS//$POLLER_SVC_NAME'
  StrCpy $WantSystray "TRUE"
SectionEnd

Section "Uninstall"
  # Deleting the kill-switch file will stop the service
  Delete "$PROFILE\.opennms\$KILL_SWITCH_FILE_NAME"

  # Try to stop the systray monitor if possible
  IfFileExists "$INSTDIR\bin\$POLLER_TRAY_FILE_NAME" 0 SkipStopSysTray
    Call un.StopSystrayMonitor
  SkipStopSysTray:

  IfFileExists "$INSTDIR\bin\$POLLER_SERVICE_FILE_NAME" 0 SkipRemoveService
    Call un.RemovePollerSvc
  SkipRemoveService:

  DetailPrint "Shutting down the $POLLER_SVC_DISP_NAME service and system tray monitor..."
  Sleep 3000
  Sleep 3000
  Sleep 3000
  Sleep 3000
  Sleep 3000
  Sleep 3000
  Sleep 3000
  Sleep 3000
  Sleep 3000
  Sleep 3000

  Push $ServiceUser
  Call un.RemoveSvcLogonRight
  Pop $1
  StrCmp $1 "OK" 0 RightRemovalFailed
    DetailPrint "Removed Log On As a Service right from user $ServiceUser"
    Goto RightRemovedOK
  RightRemovalFailed:
    DetailPrint "WARNING: Failed to remove Log On As a Service right from user $ServiceUser"
    MessageBox MB_OK|MB_ICONINFORMATION "The uninstaller was unable to remove the Log On As a Service right from user $ServiceUser.$\r$\n$\r$\nYou may wish to remove this right manually for security reasons."
  RightRemovedOK:

  Call un.RemoveSystrayMonitorStartup

  # Delete files
  Delete "$INSTDIR\agpl-3.0.txt"
  Delete "$INSTDIR\bin\$POLLER_SERVICE_FILE_NAME"
  Delete "$INSTDIR\bin\$POLLER_TRAY_FILE_NAME"
  Delete "$INSTDIR\bin\$VBS_KILL_SCRIPT"
  Delete "$INSTDIR\$UNINSTALLER_FILE_NAME"
  Sleep 1000

  # Delete directories
  RMDir /r "$INSTDIR\etc"
  RMDir /r "$INSTDIR\logs"
  RMDir /r "$INSTDIR\bin"
  RMDir "$INSTDIR"

  StrCmp $ShouldRemovePollerProps "true" 0 SkipDeletePollerProps
    Delete $POLLER_PROPS_FILE
  SkipDeletePollerProps:

  DeleteRegValue HKLM "Software\The OpenNMS Group\${PROJECT_NAME}" "InstallLocation"
  DeleteRegKey /ifempty HKLM "Software\The OpenNMS Group\${PROJECT_NAME}"
  DeleteRegKey /ifempty HKLM "Software\The OpenNMS Group"
  DeleteRegKey /ifempty HKLM "Software\Apache Software Foundation"
  Call un.RemoveAddRemProgEntry
SectionEnd

#----------------------
# Section Descriptions
#----------------------

LangString DESC_SecSystray ${LANG_ENGLISH} "Installs a system tray icon that monitors the state of the $POLLER_SVC_DISP_NAME service."

#Assign language strings to sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecSystray} $(DESC_SecSystray)
!insertmacro MUI_FUNCTION_DESCRIPTION_END



#----------------------
# Functions for OpenNMS server info page
#----------------------

Function onmsServerInfoPage
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog

  ${If} $Dialog == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 0 100% 12u "Please provide the following information about your OpenNMS server:"
  Pop $TopLabel

  ${NSD_CreateLabel} 0 20u 125u 12u "Server IP address or hostname:"
  Pop $ServerLabel

  ${NSD_CreateText} 126u 19u 100u 12u "$OnmsWebappServer"
  Pop $ServerText

  ${NSD_CreateLabel} 0 36u 125u 12u "Web UI port:"
  Pop $PortLabel

  ${NSD_CreateText} 126u 35u 30u 12u "$OnmsWebappPort"
  Pop $PortText
  ${NSD_SetTextLimit} $PortText 5

  ${NSD_CreateLabel} 0 52u 125u 12u "Web app path (not normally changed):"
  Pop $AppPathLabel

  ${NSD_CreateText} 126u 51u 50u 12u "$OnmsWebappPath"
  Pop $AppPathText

  ${NSD_CreateLabel} 0 68u 125u 12u "Web UI username:"
  Pop $UsernameLabel

  ${NSD_CreateText} 126u 67u 100u 12u "$OnmsWebappUsername"
  Pop $UsernameText

  ${NSD_CreateLabel} 0 84u 125u 12u "Web UI password:"
  Pop $PasswordLabel

  ${NSD_CreatePassword} 126u 83u 100u 12u "$OnmsWebappPassword"
  Pop $PasswordText

  ${NSD_CreateCheckBox} 0 100u 100% 12u " Use &secure connection (HTTPS)"
  Pop $HttpsCheckbox

  StrCmp $OnmsWebappProtocol "https" SetHttps SetHttp
    SetHttps:
      ${NSD_SetState} $HttpsCheckbox ${BST_CHECKED}
      Goto DoneProtocolCheckbox
    SetHttp:
      ${NSD_SetState} $HttpsCheckbox ${BST_UNCHECKED}
  DoneProtocolCheckbox:

  # It is not clear that remote debugging is supported under JWS so
  # leave this code commented out
  /*
  ${NSD_CreateCheckBox} 0 116u 100% 12u " Enable Java debugging"
  Pop $DebugCheckbox
  ${NSD_SetState} $DebugCheckbox ${BST_UNCHECKED}
  */

  nsDialogs::Show
FunctionEnd


Function onmsServerInfoPageLeave
  # Check that the server field looks kosher
  ${NSD_GetText} $ServerText $1
  StrCmp $1 "" ServerIsBlank ServerIsNotBlank
ServerIsBlank:
  MessageBox MB_OK "The server address must not be blank."
  Abort
ServerIsNotBlank:
  StrCmp $1 $DEFAULT_WEBUI_HOST ServerIsDefault ServerIsNotDefault
ServerIsDefault:
  MessageBox MB_OK "You must enter a hostname or IP address for the OpenNMS server."
  Abort
ServerIsNotDefault:
  StrCpy $2 $1 5
  StrCmp $2 "http:" ServerIsUrl
  StrCpy $2 $1 6
  StrCmp $2 "https:" ServerIsUrl
  StrCpy $2 $1 1 -1
  StrCmp $2 "/" ServerIsUrl ServerIsNotUrl
ServerIsUrl:
  MessageBox MB_OK "Please enter only a hostname or IP address, not a full URL, for the OpenNMS server."
  Abort
ServerIsNotUrl:
  StrCpy $2 $1 1
  StrCmp $2 "\" ServerIsUnc ServerIsNotUnc
ServerIsUnc:
  MessageBox MB_OK "Please enter only a hostname or IP address, not a UNC specifier, for the OpenNMS server."
  Abort
ServerIsNotUnc:

  # Check that the port is valid
  ${NSD_GetText} $PortText $1
  IntCmp $1 0 PortIsNotValid PortIsNotValid
  IntCmp $1 65535 PortIsValid PortIsValid PortIsNotValid
PortIsNotValid:
  MessageBox MB_OK "Port number must be an integer in the range 1 - 65535."
  Abort
PortIsValid:

  # Check that the webapp path looks OK
  ${NSD_GetText} $AppPathText $1
  StrCpy $2 $1 1 -1
  StrCmp $2 "/" PathTrailingSlash PathNoTrailingSlash
PathTrailingSlash:
  MessageBox MB_OK "The web app path must not have a trailing slash."
  Abort
PathNoTrailingSlash:
  ${NSD_GetText} $ServerText $OnmsWebappServer
  ${NSD_GetText} $PortText $OnmsWebappPort
  ${NSD_GetText} $AppPathText $OnmsWebappPath

  ${NSD_GetState} $HttpsCheckbox $TEMP1
  IntCmp $TEMP1 0 UseHttp UseHttps UseHttps
    UseHttp:
      StrCpy $OnmsWebappProtocol "http"
      Goto DoneCopying
    UseHttps:
      StrCpy $OnmsWebappProtocol "https"
  DoneCopying:

  # It is not clear if remote debugging is supported under JWS so
  # leave this code commented out
  /*
  ${NSD_GetState} $DebugCheckbox $TEMP1
  IntCmp $TEMP1 0 DoneDebug 0 0
    StrCpy $JnlpDebugOptions "-J-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:8001"

    StrCpy $ProcrunDebugOptions '++StartParams="-J-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:8001"'
    StrCpy $ProcrunDebugOptions '--Environment="JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:8001"'
  DoneDebug:
  */

  #MessageBox MB_OK "$OnmsWebappProtocol://$OnmsWebappServer:$OnmsWebappPort$OnmsWebappPath"
FunctionEnd


#----------------------
# Functions for the service user info page
#----------------------
Function onmsSvcUserPage
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog

  ${If} $Dialog == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 0 100% 24u "Please provide the password for the local Windows account under which the ${PROJECT_NAME} service will run."
  Pop $TopLabel

  ${NSD_CreateLabel} 0 40u 40u 12u "Username:"
  Pop $ServiceUserLabel

  ${NSD_CreateText} 61u 39u 70u 12u "$ServiceUser"
  Pop $ServiceUserText
  SendMessage $ServiceUserText ${EM_SETREADONLY} 1 0

  ${NSD_CreateLabel} 0 60u 40u 12u "Password:"
  Pop $ServicePasswordLabel

  ${NSD_CreatePassword} 61u 59u 70u 12u "$ServicePassword"
  Pop $ServicePasswordText

  ${NSD_CreateLabel} 0 80u 60u 12u "Repeat Password:"
  Pop $ServicePasswordRepeatLabel

  ${NSD_CreatePassword} 61u 79u 70u 12u "$ServicePassword"
  Pop $ServicePasswordRepeatText

  nsDialogs::Show
FunctionEnd

Function onmsSvcUserPageLeave
  Push $0
  Push $1
  Push $2
  ${NSD_GetText} $ServicePasswordText $1
  ${NSD_GetText} $ServicePasswordRepeatText $2
  StrCmp $1 $2 PasswordsMatch
    MessageBox MB_OK "The password fields must match.  Please try again."
    Abort
  PasswordsMatch:
  StrCpy $ServicePassword $1
  Pop $2
  Pop $1
  Pop $0
FunctionEnd

#----------------------
# Functions for the GUI poller launch page
#----------------------
Function launchGuiPollerPage
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog

  ${If} $Dialog == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 0 100% 100% "Done copying files.$\r$\n$\r$\nThe installer will now download and start the GUI version of the Remote Poller.  This may take a few minutes to complete.$\r$\n$\r$\nIf prompted whether always to trust content from this publisher, please answer affirmatively and click Run.$\r$\n$\r$\nIf the Remote Poller has never been installed on this computer, then you will need to register the Remote Poller with the OpenNMS server.$\r$\n$\r$\nClick Next to launch the Remote Poller GUI."
  Pop $LaunchGuiLabel

  nsDialogs::Show
FunctionEnd

Function launchGuiPollerPageLeave
  Call PurgeJWSCache
  Call LaunchGUIPoller
FunctionEnd


#----------------------
# Functions for the service creation page
#----------------------
Function svcCreationPage
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog

  ${If} $Dialog == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 0 100% 100% "The GUI Remote Poller is downloading and launching.  Please be patient as this process may take a few minutes.$\r$\n$\r$\nAfter the Remote Poller has successfully registered with the OpenNMS server, please close the Remote Poller window and click Next to continue.$\r$\n$\r$\nIf the Remote Poller window has not appeared after a few moments, please cancel this installation and contact the person in your organization responsible for administering OpenNMS."
  Pop $TopLabel

  nsDialogs::Show  
FunctionEnd

Function svcCreationPageLeave
  Call IsGUIPollerRunning
  Pop $1
  StrCmp $1 "OK" PollerRunning PollerNotRunning
PollerRunning:
  MessageBox MB_OK "The Remote Poller window still appears to be open.$\r$\nPlease close the Remote Poller window and try again."
  Abort
PollerNotRunning:
  IfFileExists $POLLER_PROPS_FILE PropsExists PropsNotExists
PropsNotExists:
  MessageBox MB_OK "The Remote Poller does not seem to have successfully registered with the OpenNMS server.$\r$\n$\r$\nPlease click Back to try again.$\r$\n$\r$\nIf this message persists, please cancel this installation and contact the person in your organization responsible for administering OpenNMS."
PropsExists:
  Call CreateOrUpdatePollerSvc
  Pop $1
  StrCmp $1 "OK" ServiceOK ServiceFail
ServiceFail:
  MessageBox MB_OK|MB_ICONEXCLAMATION "The attempt to install or update the Remote Poller service failed.$\r$\n$\r$\nPlease go back and double-check the password for the $serviceUser user.$\r$\n$\r$\nIf this message persists, please cancel this installation and contact the person in your organization responsible for administering OpenNMS."
  Abort
ServiceOK:
  Delete "$PROFILE\.opennms\$KILL_SWITCH_FILE_NAME"
FunctionEnd


Function svcStartPage
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog

  ${If} $Dialog == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 0 100% 100% "The installer will now attempt to start the $POLLER_SVC_DISP_NAME service.$\r$\n$\r$\nIf this step fails, please contact the person in your organization responsible for administering OpenNMS."
  Pop $TopLabel

  nsDialogs::Show

  # Add log-on-as-service privilege to service user
  Push $ServiceUser
  Call AddSvcLogonRight
  Pop $1
  StrCmp $1 "OK" AddPrivOK AddPrivFail

AddPrivFail:
  DetailPrint "Failed to add Log On As a Service right to user $ServiceUser, aborting"
  MessageBox MB_OK|MB_ICONEXCLAMATION "The attempt to add the Log On As a Service right to user $ServiceUser failed.$\r$\n$\r$\nPlease contact the person in your organization responsible for administering OpenNMS."
  Abort
AddPrivOK:
  DetailPrint "Added Log On As a Service right for user $ServiceUser"

  DetailPrint "Attempting to start $POLLER_SVC_NAME service..."
  ExecWait '"$SYSDIR\net.exe" start $POLLER_SVC_NAME' $1
  Pop $1
  IntCmp $1 0 SvcStartOK SvcStartFail SvcStartFail
SvcStartFail:
  DetailPrint "Service $POLLER_SVC_NAME failed to start, exit code $1"
  MessageBox MB_OK|MB_ICONEXCLAMATION "The $POLLER_SVC_DISP_NAME service failed to start.  Please contact the person in your organization responsible for administering OpenNMS."
  Abort
SvcStartOK:
  # Launch the systray monitor if wanted and not already running
  StrCmp $WantSystray "TRUE" DoSystray SkipSystray
DoSystray:
  Call LaunchSystrayMonitor
SkipSystray:
FunctionEnd


#----------------------
# Page function to check whether we have a valid Java
#----------------------
Function javaCheckPage
  StrCpy $JavaHome ""
  # See how many Java candidates we have
  # A 1 on the stack means "push the candidates too"
  Push 1
  Call GetJavaHomeCandidates
  Pop $TEMP1
  StrCmp $TEMP1 "0" 0 NotZeroJava
    MessageBox MB_OK|MB_ICONEXCLAMATION "Java could not be found on this system.$\r$\nPlease install Java 1.8 before continuing."
    Abort
  NotZeroJava:

  StrCmp $TEMP1 "1" 0 NotExactlyOneJava
    Pop $TEMP2
    StrCpy $JavaHome $TEMP2

    # We have a sure thing here, don't even show the Java listbox
    Abort

  NotExactlyOneJava:
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog

  ${If} $Dialog == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 10u 100% 30u "The installer has identified multiple Java installations on this system, but could not$\r$\ndetermine the best one to use.  Please select the Java installation to use for$\r$\n$POLLER_SVC_DISP_NAME."
  Pop $ServiceUserLabel

  ${NSD_CreateListBox} 0 40u 100% 80u "List of suitable Java installations"
  Pop $JavaListBox

  # In this loop, $TEMP2 is the iterator and $TEMP1 is the max iterator value
  StrCpy $TEMP2 0
  AddJavaChoiceLoop:
    Pop $TEMP3
    # Only add the choice if it's different than the previous value to reduce duplicates
    StrCmp $TEMP3 $TEMP4 DoneAdd 0
      ${NSD_LB_AddString} $JavaListBox $TEMP3
    DoneAdd:
    StrCpy $TEMP4 $TEMP3
    IntOp $TEMP2 $TEMP2 + 1
    StrCmp $TEMP2 $TEMP1 EndJavaChoiceLoop AddJavaChoiceLoop
  EndJavaChoiceLoop:

  nsDialogs::Show
FunctionEnd

Function javaCheckPageLeave
  StrCmp $JavaHome "" 0 JavaSelected
    ${NSD_LB_GetSelection} $JavaListBox $1
    StrCmp $1 "" 0 JavaSelected
    MessageBox MB_OK|MB_ICONEXCLAMATION "You must choose a Java installation."
    Abort
  JavaSelected:
  StrCpy $JavaHome $1
FunctionEnd


#----------------------
# Functions for install page that prompts whether
# to remove an existing poller properties file
#----------------------
Function removeOldRegFilePage
  StrCmp $POLLER_PROPS_FILE "" 0 CheckPropsFileExists
    Abort
  CheckPropsFileExists:
  IfFileExists $POLLER_PROPS_FILE ProceedOptions 0
    Abort
  ProceedOptions:
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog
  ${If} $Dialog == error
    Abort
  ${EndIf}
  ${NSD_CreateLabel} 0 10u 100% 36u "The Remote Poller on this system has previously registered with an OpenNMS server.  Do you want to keep the existing registration or remove it and create a new one?  If you remove it, the continuity of data gathered by the poller on this system will be interrupted."
  Pop $ServiceUserLabel
  ${NSD_CreateRadioButton} 0 50u 100% 12u "&Keep existing registration"
  Pop $TEMP1
  ${NSD_SetState} $TEMP1 ${BST_CHECKED}
  ${NSD_CreateRadioButton} 0 64u 100% 12u "&Delete existing registration and create a new one"
  Pop $ShouldRemovePollerProps
  ${NSD_SetState} $ShouldRemovePollerProps ${BST_UNCHECKED}
  nsDialogs::Show
FunctionEnd

Function removeOldRegFilePageLeave
  ${NSD_GetState} $ShouldRemovePollerProps $TEMP1
  StrCmp $TEMP1 ${BST_CHECKED} 0 DontRemoveProps
    Delete $POLLER_PROPS_FILE
  DontRemoveProps:
FunctionEnd



#----------------------
# Functions for uninstall options page
#----------------------
Function un.OptionsPage
  StrCmp $POLLER_PROPS_FILE "" 0 CheckPropsFileExists
    Abort
  CheckPropsFileExists:
  IfFileExists $POLLER_PROPS_FILE ProceedUnOptions 0
    Abort
  ProceedUnOptions:
  nsDialogs::Create /NOUNLOAD 1018
  Pop $Dialog
  ${If} $Dialog == error
    Abort
  ${EndIf}
  ${NSD_CreateLabel} 0 10u 100% 30u "Do you want to remove the Remote Poller registration file?  If you remove this file and later reinstall $POLLER_SVC_DISP_NAME on this system, the continuity of data gathered by the poller on this system will be interrupted."
  Pop $ServiceUserLabel
  ${NSD_CreateCheckBox} 0 50u 100% 12u "Remove Remote Poller &registration file"
  Pop $ShouldRemovePollerProps
  nsDialogs::Show
FunctionEnd

Function un.OptionsPageLeave
  ${NSD_GetState} $ShouldRemovePollerProps $TEMP1
  StrCmp $TEMP1 ${BST_CHECKED} 0 DontRemoveProps
    StrCpy $ShouldRemovePollerProps "true"
  DontRemoveProps:
FunctionEnd


#----------------------
# Function to return a list of candidate JAVA_HOMEs
# Pushes all candidate paths onto the stack, followed
# by a number of paths pushed.  Callers should first pop
# the number (N), and then pop N more values, each of
# which is a candidate path.
#----------------------
Function GetJavaHomeCandidates
  # $TEMP0  tells us whether to push the candidate names
  Pop $TEMP0
  # $TEMP1 will be our path counter
  StrCpy $TEMP1 0

  # Search for the default javaws.exe in Microsoft\Windows\CurrentVersion\App Paths, if set
  ReadRegStr $TEMP2 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\javaws.exe" "Path"
  # If the key doesn't exist, skip
  StrCmp $TEMP2 "" DoneJWSCurrent
    # If the file doesn't exist, skip
    IfFileExists $TEMP2 0 DoneJWSCurrent

    # Chop off 4 chars ("\bin") from the end to get a JAVA_HOME
    StrLen $TEMP3 $TEMP2
    IntOp $TEMP3 $TEMP2 - 4
    StrCpy $TEMP2 $TEMP2 $TEMP3

    #LogText "Found javaws.exe via CurrentVersion registry at: $TEMP2"
    IntOp $TEMP1 $TEMP1 + 1
    StrCmp $TEMP0 "1" 0 SkipPushJWSCurrent
      Push $TEMP2
    SkipPushJWSCurrent:

    # If we found this one, stop looking
    #Push $TEMP1
    #Return
  DoneJWSCurrent:

  # Check for a Java Web Start registry entry
  ReadRegStr $TEMP2 HKLM "SOFTWARE\JavaSoft\Java Web Start" "CurrentVersion"
  StrCmp $TEMP2 "" DoneJWS
    ReadRegStr $TEMP2 HKLM "SOFTWARE\JavaSoft\Java Web Start\$TEMP2" "Home"
    StrCmp $TEMP2 "" DoneJWS
      IfFileExists "$TEMP2\..\*.*" 0 DoneJWS
      IfFileExists "$TEMP2\javaws.exe" 0 DoneJWS

      # Chop off 4 chars ("\bin") from the end to get a JAVA_HOME
      StrLen $TEMP3 $TEMP2
      IntOp $TEMP3 $TEMP2 - 4
      StrCpy $TEMP2 $TEMP2 $TEMP3

      #LogText "Found javaws.exe via Java Web Start registry at: $TEMP2"
      IntOp $TEMP1 $TEMP1 + 1
      StrCmp $TEMP0 "1" 0 SkipPushJWS
        Push $TEMP2
      SkipPushJWS:
  DoneJWS:

  # Check for Java Runtime Environment registry entries
  # TEMP2 will be our iterator within this loop
  StrCpy $TEMP2 0
  LoopJRE:
    EnumRegKey $TEMP3 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" $TEMP2
    StrCmp $TEMP3 "" DoneJRE

    # Check that it's a 1.8 JRE
    StrCpy $TEMP4 $TEMP3 3
    StrCmp $TEMP4 "1.8" 0 NextJRE
      ReadRegStr $TEMP3 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$TEMP3" "JavaHome"
      StrCmp $TEMP3 "" NextJRE 0
        IfFileExists "$TEMP3\bin\javaws.exe" 0 NextJRE

        #LogText "Found javaws.exe via Java Runtime Environment registry at: $TEMP3"
        IntOp $TEMP1 $TEMP1 + 1
        StrCmp $TEMP0 "1" 0 SkipPushJRE
          Push $TEMP3
        SkipPushJRE:
    NextJRE:
      IntOp $TEMP2 $TEMP2 + 1
    Goto LoopJRE
  DoneJRE:

  # Check for Java Development Kit registry entries
  #
  # NOTE: This is commented out because running from the JDK paths fails on 
  # recent versions of JDK 1.8 (1.8.0u151)
  /*
  # TEMP2 will be our iterator within this loop
  StrCpy $TEMP2 0
  LoopJDK:
    EnumRegKey $TEMP3 HKLM "SOFTWARE\JavaSoft\Java Development Kit" $TEMP2
    StrCmp $TEMP3 "" DoneJDK

    # Check that it's a 1.8 JRE
    StrCpy $TEMP4 $TEMP3 3
    StrCmp $TEMP4 "1.8" 0 NextJDK
      ReadRegStr $TEMP3 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$TEMP3" "JavaHome"
      StrCmp $TEMP3 "" NextJDK 0
        IfFileExists "$TEMP3\jre\bin\javaws.exe" 0 NextJDK

        #LogText "Found javaws.exe via Java Development Kit registry at: $TEMP3\jre"
        IntOp $TEMP1 $TEMP1 + 1
        StrCmp $TEMP0 "1" 0 SkipPushJDK
          Push "$TEMP3\jre"
        SkipPushJDK:
    NextJDK:
      IntOp $TEMP2 $TEMP2 + 1
    Goto LoopJDK
  DoneJDK:
  */

  # Push the counter
  Push $TEMP1
  Return
FunctionEnd


#----------------------
# Function to get the Java Web Start executable
#----------------------
Function GetJWS
  StrCpy $TEMP4 "$JavaHome\bin\javaws.exe"
  Push $TEMP4
  Return
FunctionEnd


#----------------------
# Function that builds the JWS base URL
#----------------------
Function GetJWSBaseURL
  StrCpy $TEMP0 "$OnmsWebappProtocol://$OnmsWebappServer:$OnmsWebappPort$OnmsWebappPath/webstart"
  Push "$TEMP0"
  #MessageBox MB_OK "Using JNLP URL: $TEMP0"
  Return
FunctionEnd


#----------------------
# Function that launches the GUI poller
#----------------------
Function LaunchGUIPoller
  Call GetJWS
  Pop $JWSEXE
  Call GetJWSBaseURL
  Pop $1
  StrCpy $JnlpUrl "$1/$GUI_POLLER_JNLP"
  Exec '"$JWSEXE" $JnlpDebugOptions -J-Djnlp.opennms.poller.killSwitch.resource=$PROFILEJAVA/.opennms/$KILL_SWITCH_FILE_NAME -J-Djnlp.log4j.configurationFile=file:///$INSTDIRJAVA/etc/log4j2.xml -J-Djnlp.opennms.poller.server.username=$OnmsWebappUsername -J-Djnlp.opennms.poller.server.password=$OnmsWebappPassword $JnlpUrl'
  IntCmp $1 0 GuiInstallOk
    MessageBox MB_OK|MB_ICONEXCLAMATION "The GUI installer did not complete successfully. Please check the configuration parameters and try to launch it again."
  GuiInstallOk:
FunctionEnd


#----------------------
# Function that creates or updates the headless poller service
#----------------------
Function CreateOrUpdatePollerSvc
  Call GetJWS
  Pop $JWSEXE
  Call GetJWSBaseURL
  Pop $1
  StrCpy $JnlpUrl "$1/$HEADLESS_POLLER_JNLP"
  #StrCpy $ProcrunJWSOptions "-wait#-Xnosplash#-J-Djnlp.opennms.poller.killSwitch.resource=$PROFILEJAVA/.opennms/$KILL_SWITCH_FILE_NAME#-J-Djnlp.log4j.configurationFile=file:///$INSTDIRJAVA/etc/log4j2.xml#-J-Djnlp.opennms.poller.server.username=$OnmsWebappUsername#-J-Djnlp.opennms.poller.server.password=$OnmsWebappPassword#$ProcrunDebugOptions"
  StrCpy $ProcrunJWSOptions '--StartParams="-wait" ++StartParams="-Xnosplash" ++StartParams="-J-Djnlp.opennms.poller.killSwitch.resource=$PROFILEJAVA/.opennms/$KILL_SWITCH_FILE_NAME" ++StartParams="-J-Djnlp.log4j.configurationFile=file:///$INSTDIRJAVA/etc/log4j2.xml" ++StartParams="-J-Djnlp.opennms.poller.server.username=$OnmsWebappUsername" ++StartParams="-J-Djnlp.opennms.poller.server.password=$OnmsWebappPassword" $ProcrunDebugOptions ++StartParams="$JnlpUrl"'

  # Check whether the service exists, decide on our verb (install / update) accordingly
  Push $POLLER_SVC_NAME
  Call CheckSvcExists
  Pop $1
  StrCmp $1 "OK" VerbUpdate VerbInstall
VerbInstall:
  StrCpy $1 "IS"
  Goto DoService
VerbUpdate:
  StrCpy $1 "US"
  Goto DoService
DoService:
  ExecWait '"$INSTDIR\bin\$POLLER_SERVICE_FILE_NAME" //$1//$POLLER_SVC_NAME --DisplayName="$POLLER_SVC_DISP_NAME" --Description="$POLLER_SVC_DESCRIPTION"  --DependsOn="Tcpip" --StartMode exe --StopMode exe --ServiceUser=".\$ServiceUser" --ServicePassword="$ServicePassword" --StartImage="$JWSEXE" $ProcrunJWSOptions' $1
  IntCmp $1 0 CreateOK CreateFail CreateFail
CreateFail:
  MessageBox MB_OK|MB_ICONEXCLAMATION "Service creation failed"
  Push "NOK"
  Return
CreateOK:
  GetFullPathName /SHORT $1 "$INSTDIR\bin\$VBS_KILL_SCRIPT"
  # Note that it is pointless to use '--StdOutput=auto --StdError=auto' here because we are executing Java Web Start which redirects all stdout/stderr to the Java console
  ExecWait '"$INSTDIR\bin\$POLLER_SERVICE_FILE_NAME" //US//$POLLER_SVC_NAME  --StopImage="$SYSDIR\wscript.exe" --StopParams="//B#//NOLOGO#$1" --LogLevel=DEBUG --LogPath="$INSTDIR\logs" --LogPrefix=procrun --Startup=auto' $1
  IntCmp $1 0 UpdateOK UpdateFail UpdateFail
  Goto UpdateOK
UpdateFail:
  MessageBox MB_OK|MB_ICONEXCLAMATION "Update failed"
  Push "NOK"
  Return
UpdateOK:
  Push "OK"
  Return
FunctionEnd


#----------------------
# Function that removes the headless poller service
#----------------------
Function un.RemovePollerSvc
  Push $POLLER_SVC_NAME
  Call un.CheckSvcExists
  Pop $1
  StrCmp $1 "OK" 0 SkipRemoval
    ExecWait '"$INSTDIR\bin\$POLLER_SERVICE_FILE_NAME" //DS//$POLLER_SVC_NAME' $1
    IntCmp $1 0 Done
      MessageBox MB_OK|MB_ICONEXCLAMATION "System tray service deletion failed."
    Done:
  SkipRemoval:
FunctionEnd


#----------------------
# Function that launches the systray monitor if it is not already running
#----------------------
Function LaunchSystrayMonitor
  Exec '"$INSTDIR\bin\$POLLER_TRAY_FILE_NAME" //MS//$POLLER_SVC_NAME'
FunctionEnd



#----------------------
# Function that stops the systray monitor if it is running
#----------------------
Function StopSystrayMonitor
  ExecWait '"$INSTDIR\bin\$POLLER_TRAY_FILE_NAME" //MQ//$POLLER_SVC_NAME' $1
  IntCmp $1 0 Done
    MessageBox MB_OK|MB_ICONEXCLAMATION "System tray stop failed."
  Done:
FunctionEnd



#----------------------
# Function that stops the systray monitor if it is running (uninstaller copy, keep synchronized!)
#----------------------
Function un.StopSystrayMonitor
  ExecWait '"$INSTDIR\bin\$POLLER_TRAY_FILE_NAME" //MQ//$POLLER_SVC_NAME' $1
  IntCmp $1 0 Done
    MessageBox MB_OK|MB_ICONEXCLAMATION "System tray stop failed."
  Done:
FunctionEnd



#----------------------
# Function that removes the systray monitor from Windows startup
#----------------------
Function un.RemoveSystrayMonitorStartup
  DeleteRegValue HKLM "Software\Microsoft\Windows\CurrentVersion\Run" "$POLLER_SVC_NAME"
FunctionEnd



#----------------------
# Does the named service exist?
#----------------------
Function CheckSvcExists
  Pop $TEMP2
  StrCpy $TEMP0 0
  loop:
    EnumRegKey $TEMP1 HKLM "SYSTEM\CurrentControlSet\Services" $TEMP0
    StrCmp $TEMP1 $TEMP2 FoundSvc
    StrCmp $TEMP1 "" NotFound
    IntOp $TEMP0 $TEMP0 + 1
    Goto loop
  FoundSvc:
    Push "OK"
    Return
  NotFound:
    Push "NOK"
    Return
FunctionEnd


#----------------------
# Does the named service exist? (uninstaller copy, keep synchronized!)
#----------------------
Function un.CheckSvcExists
  Pop $TEMP2
  StrCpy $TEMP0 0
  loop:
    EnumRegKey $TEMP1 HKLM "SYSTEM\CurrentControlSet\Services" $TEMP0
    StrCmp $TEMP1 $TEMP2 FoundSvc
    StrCmp $TEMP1 "" NotFound
    IntOp $TEMP0 $TEMP0 + 1
    Goto loop
  FoundSvc:
    Push "OK"
    Return
  NotFound:
    Push "NOK"
    Return
FunctionEnd



#----------------------
# Function that checks whether the GUI Remote Poller window is open
#----------------------
Function IsGUIPollerRunning
  FindWindow $TEMP1 "" "OpenNMS Remote Poller"
  IntCmp $TEMP1 0 IsNotRunning
    Push "OK"
    Return
  IsNotRunning:
    Push "NOK"
    Return
FunctionEnd


#----------------------
# Function that turns a backslashed pathname into a foreslashed one (ouch!)
#----------------------
Function MkJavaPath
  Var /GLOBAL BSStringLong
  Var /GLOBAL BSString
  Var /GLOBAL FSString
  Var /GLOBAL ThisChar
  Var /GLOBAL BSLength
  Var /GLOBAL CurPos

  # Start clean since we'll be used repeatedly.
  StrCpy $BSString ""
  StrCpy $FSString ""
  StrCpy $ThisChar ""
  StrCpy $BSLength "0"
  StrCpy $CurPos "0"

  Pop $BSStringLong
  GetFullPathName /SHORT $BSString $BSStringLong
  StrLen $BSLength $BSString
  StrCpy $CurPos "0"

loop:
    IntCmp $CurPos $BSLength done
    StrCpy $ThisChar $BSString 1 $CurPos
    StrCmp $ThisChar "\" DoSubst DontSubst
DoSubst:
    StrCpy $FSString "$FSString/"
    Goto SkipChar
DontSubst:
    StrCpy $FSString "$FSString$ThisChar"
SkipChar:
    IntOp $CurPos $CurPos + 1
    Goto loop
done:
  Push $FSString
FunctionEnd


#----------------------
# Function that writes a customized log4j2.xml file
#
# This should be kept in sync with the console-only version stored
# at features/remote-poller/src/main/resources/log4j2.xml.
#----------------------
Function WriteCustomLogPropsFile
  Push $0
  FileOpen $0 $INSTDIR\etc\log4j2.xml w
  FileWrite $0 "<?xml version=$\"1.0$\" encoding=$\"UTF-8$\"?>$\r$\n"
  FileWrite $0 "<!-- WARN here is just for internal log4j messages and does not effect logging in general -->$\r$\n"
  FileWrite $0 "<configuration status=$\"WARN$\" monitorInterval=$\"60$\">$\r$\n"
  FileWrite $0 "  <properties>$\r$\n"
  FileWrite $0 "    <property name=$\"prefix$\">opennms-remote-poller</property>$\r$\n"
  FileWrite $0 "    <property name=$\"poller.logfile$\">$INSTDIRJAVA/logs</property>$\r$\n"
  FileWrite $0 "  </properties>$\r$\n"
  FileWrite $0 "  <appenders>$\r$\n"
  FileWrite $0 "    <Console name=$\"ConsoleAppender$\" target=$\"SYSTEM_OUT$\">$\r$\n"
  FileWrite $0 "      <PatternLayout pattern=$\"%d %-5p [%t] %c{1.}: %m%n$\"/>$\r$\n"
  FileWrite $0 "    </Console>$\r$\n"
  FileWrite $0 "    <Routing name=$\"RoutingAppender$\">$\r$\n"
  FileWrite $0 "      <Routes pattern=$\"$$$${ctx:prefix}$\">$\r$\n"
  FileWrite $0 "        <Route>$\r$\n"
  FileWrite $0 "          <RollingFile name=$\"Rolling-$${ctx:prefix}$\" fileName=$\"$${poller.logfile}/$${ctx:prefix}.log$\" filePattern=$\"$${poller.logfile}/$${ctx:prefix}.%i.log.gz$\">$\r$\n"
  FileWrite $0 "            <PatternLayout>$\r$\n"
  FileWrite $0 "              <pattern>%d %-5p [%t] %c{1.}: %m%n</pattern>$\r$\n"
  FileWrite $0 "            </PatternLayout>$\r$\n"
  FileWrite $0 "            <SizeBasedTriggeringPolicy size=$\"10MB$\" />$\r$\n"
  FileWrite $0 "            <DefaultRolloverStrategy max=$\"4$\" fileIndex=$\"min$\" />$\r$\n"
  FileWrite $0 "          </RollingFile>$\r$\n"
  FileWrite $0 "        </Route>$\r$\n"
  FileWrite $0 "      </Routes>$\r$\n"
  FileWrite $0 "    </Routing>$\r$\n"
  FileWrite $0 "  </appenders>$\r$\n"
  FileWrite $0 "  <loggers>$\r$\n"
  FileWrite $0 "    <!--$\r$\n"
  FileWrite $0 "      Set the threshold for individual loggers that may be too chatty at the default$\r$\n"
  FileWrite $0 "      level for their prefix.$\r$\n"
  FileWrite $0 "    -->$\r$\n"
  FileWrite $0 "    <logger name=$\"httpclient$\" additivity=$\"false$\" level=$\"INFO$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <logger name=$\"net.sf.jasperreports$\" additivity=$\"false$\" level=$\"INFO$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <logger name=$\"org.apache.bsf$\" additivity=$\"false$\" level=$\"INFO$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <logger name=$\"org.apache.commons$\" additivity=$\"false$\" level=$\"WARN$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <logger name=$\"org.asteriskjava$\" additivity=$\"false$\" level=$\"WARN$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <logger name=$\"org.eclipse.jetty.webapp$\" additivity=$\"false$\" level=$\"INFO$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <logger name=$\"org.quartz$\" additivity=$\"false$\" level=$\"INFO$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <logger name=$\"org.springframework$\" additivity=$\"false$\" level=$\"INFO$\"><appender-ref ref=$\"ConsoleAppender$\"/><appender-ref ref=$\"RoutingAppender$\"/></logger>$\r$\n"
  FileWrite $0 "    <!-- Allow any message to pass through the root logger -->$\r$\n"
  FileWrite $0 "    <root level=$\"DEBUG$\">$\r$\n"
  FileWrite $0 "      <appender-ref ref=$\"ConsoleAppender$\"/>$\r$\n"
  FileWrite $0 "      <appender-ref ref=$\"RoutingAppender$\"/>$\r$\n"
  FileWrite $0 "    </root>$\r$\n"
  FileWrite $0 "  </loggers>$\r$\n"
  FileWrite $0 "</configuration>$\r$\n"
  FileClose $0
  Pop $0
FunctionEnd


#----------------------
# Function that writes a customized VBScript that deletes the
# Remote Poller kill-switch file
#----------------------
Function WriteCustomVbsKillScript
  Push $0
  Push $1
  GetFullPathName /SHORT $1 "$PROFILE"
  FileOpen $0 $INSTDIR\bin\$VBS_KILL_SCRIPT w

  FileWrite $0 'Call DeleteAFile("$1\.opennms\$KILL_SWITCH_FILE_NAME")$\r$\n'
  FileWrite $0 '$\r$\n'
  FileWrite $0 'Sub DeleteAFile(filespec)$\r$\n'
  FileWrite $0 '   Dim fso$\r$\n'
  FileWrite $0 '   Set fso = CreateObject("Scripting.FileSystemObject")$\r$\n'
  FileWrite $0 '   fso.DeleteFile(filespec)$\r$\n'
  FileWrite $0 'End Sub$\r$\n'
  FileClose $0
  Pop $0  
FunctionEnd



#----------------------
# Function that writes uninstaller info to the Add/Remove Programs
# Control Panel menu
#----------------------
Function WriteAddRemProgEntry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "DisplayName" "$POLLER_SVC_DISP_NAME Service"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "UninstallString" '"$INSTDIR\$UNINSTALLER_FILE_NAME"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "InstallLocation" "$INSTDIR"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "DisplayIcon" "$INSTDIR\bin\$POLLER_SERVICE_FILE_NAME"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "Publisher" "The OpenNMS Group, Inc."
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "HelpLink" "http://www.opennms.org/"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME" "NoRepair" 1
FunctionEnd


#----------------------
# Function that removes uninstaller info from the Add/Remove Programs
# Control Panel menu
#----------------------
Function un.RemoveAddRemProgEntry
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$POLLER_SVC_NAME"
FunctionEnd


#----------------------
# Function that purges the Web Start cache for the service user
#----------------------
Function PurgeJWSCache
  Call GetJWS
  Pop $JWSEXE
  ExecWait "$JWSEXE -uninstall" $1
  StrCmp $1 "0" PurgeOK
    MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to purge the Java Web Start cache. You may continue, but the GUI poller may not launch properly."
  PurgeOK:
  Return
FunctionEnd


#----------------------
# Function that adds the "log on as a service" right to a named local user account
#----------------------
Function AddSvcLogonRight
  # Pop the username off the stack
  Pop $2
  System::Call '*${strLSA_OBJECT_ATTRIBUTES}(24,n,n,0,n,n).r0'
  System::Call 'advapi32::LsaOpenPolicy(w n, i r0, i ${POLICY_LOOKUP_NAMES_CREATE_ACCOUNT}, *i .R0) i.R8'
  StrCpy $3 ${NSIS_MAX_STRLEN}
  System::Call '*(&w${NSIS_MAX_STRLEN})i.R1'
  System::Call 'Advapi32::LookupAccountNameW(w n, w r2, i R1, *i r3, w .R8, *i r3, *i .r4) i .R8'
 
  # Add the rights
  StrCpy $2 "SeServiceLogonRight"
  System::Call '*${strLSA_UNICODE_STRING}(38,38,r2).s'
  Pop $R2

  System::Call 'advapi32::LsaAddAccountRights(i R0, i R1, i R2, i 1)i.R8'
  System::Call 'advapi32::LsaNtStatusToWinError(i R8) i.R9'

  # Check the WinError code for success
  IntCmp $R9 0 AddOK AddFail AddFail
AddOK:
  Push "OK"
  Goto DoAddCleanup
AddFail:
  Push "NOK"
DoAddCleanup:
  System::Free $0
  System::Free $R1
  System::Call 'advapi32::LsaFreeMemory(i R2) i .R8'
  System::Call 'advapi32::LsaClose(i R0) i .R8'
FunctionEnd


#----------------------
# Function that removes the "log on as a service" right from a named local user account
#----------------------
Function un.RemoveSvcLogonRight
  # Pop the username off the stack
  Pop $2
  System::Call '*${strLSA_OBJECT_ATTRIBUTES}(24,n,n,0,n,n).r0'
  System::Call 'advapi32::LsaOpenPolicy(w n, i r0, i ${POLICY_LOOKUP_NAMES_CREATE_ACCOUNT}, *i .R0) i.R8'
  StrCpy $3 ${NSIS_MAX_STRLEN}
  System::Call '*(&w${NSIS_MAX_STRLEN})i.R1'
  System::Call 'Advapi32::LookupAccountNameW(w n, w r2, i R1, *i r3, w .R8, *i r3, *i .r4) i .R8'
 
  # Remove the rights
  StrCpy $2 "SeServiceLogonRight"
  System::Call '*${strLSA_UNICODE_STRING}(38,38,r2).s'
  Pop $R2

  System::Call 'advapi32::LsaRemoveAccountRights(i R0, i R1, i 0, i R2, i 1)i.R8'
  System::Call 'advapi32::LsaNtStatusToWinError(i R8) i.R9'

  # Check the WinError code for success
  IntCmp $R9 0 RemoveOK RemoveFail RemoveFail
RemoveOK:
  Push "OK"
  Goto DoRemoveCleanup
RemoveFail:
  Push "NOK"
DoRemoveCleanup:
  System::Free $0
  System::Free $R1
  System::Call 'advapi32::LsaFreeMemory(i R2) i .R8'
  System::Call 'advapi32::LsaClose(i R0) i .R8'
FunctionEnd
