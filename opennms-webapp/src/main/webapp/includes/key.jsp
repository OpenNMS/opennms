<p class="key"><img src="images/key.png" alt="openNMS status colour key" usemap="#keymap" />Legend</p>
<map id="keymap" name="keymap">
	<area shape="rect" coords="0,0,15,15" title="${params.clearedCaption == null ? 'CLEARED' : params.clearedCaption}"/>
	<area shape="rect" coords="16,0,31,15" title="${params.normalCaption == null ? 'NORMAL :  Informational message. No action required.' : params.normalParam}" />
	<area shape="rect" coords="32,0,47,15" title="${params.indetermCaption == null ? 'INDETERMINATE - No severity could be associated.' : params.indetermParam}" />
	<area shape="rect" coords="48,0,63,15" title="${params.warnParam == null ? 'WARNING - May require action. Should possibly be logged.' : params.warnParam}" />
	<area shape="rect" coords="64,0,79,15" title="${params.minorParam == null ? 'MINOR - Part of a device (service, interface, power supply etc.) has stopped. Attention required.' : params.minorParam}" />
	<area shape="rect" coords="80,0,95,15" title="${params.majorParam == null ? 'MAJOR - Device completely down or in danger of going down. Immediate attention required.' : params.majorParam}" />
	<area shape="rect" coords="96,0,111,15" title="${params.criticalParam == null ? 'CRITICAL - Numerous devices are affected, fixing the problem is essential.' : params.criticalParam}" />
</map>
