<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink :icon="logo" title="Home" type="home" url="/" />
      <span class="body-large">2022-09-28T16:08:54-04:00</span>
      <font-awesome-icon
        icon="fa-solid fa-bell-slash"
        class="alarm-error"
        title="Notices: Off"
      ></font-awesome-icon>
      <Search v-if="!route.fullPath.includes('/map')" />
     </template>

    <template v-slot:right>
      <a href="/search" class="menu-link">Search</a>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            Info
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem><a href="/opennms/element/nodeList.htm" class="dropdown-menu-link">Nodes</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/asset/index.jsp" class="dropdown-menu-link">Assets</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/pathOutage/index.jsp" class="dropdown-menu-link">Path Outages</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/ui/index.html#/device-config-backup" class="dropdown-menu-link">Device Configs</a></FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            Status
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem><a href="/opennms/event/index" class="dropdown-menu-link">Events</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/alarm/index.htm" class="dropdown-menu-link">Alarms</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/notification/index.jsp" class="dropdown-menu-link">Notifications</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/outage/index.jsp" class="dropdown-menu-link">Outages</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/surveillance-view.jsp" class="dropdown-menu-link">Surveillance</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/heatmap/index.jsp" class="dropdown-menu-link">Heatmap</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/trend/index.jsp" class="dropdown-menu-link">Trend</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/application/index.jsp" class="dropdown-menu-link">Application</a></FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            Reports
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem><a href="/opennms/charts/index.jsp" class="dropdown-menu-link">Charts</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/graph/index.jsp" class="dropdown-menu-link">Resource Graphs</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/KSC/index.jsp" class="dropdown-menu-link">KSC Reports</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/report/database/index.jsp" class="dropdown-menu-link">Database Reports</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/statisticsReports/index.htm" class="dropdown-menu-link">Statistics</a></FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            Dashboards
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem><a href="/opennms/dashboard.jsp" class="dropdown-menu-link">Dashboard</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/vaadin-wallboard" class="dropdown-menu-link">Ops Board</a></FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            Maps
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem><a href="/opennms/topology" class="dropdown-menu-link">Topology</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/node-maps" class="dropdown-menu-link">Geographical</a></FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="#" v-bind="attrs" v-on="on">
            Help
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem><a href="/opennms/help/index.jsp" class="dropdown-menu-link">Help</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/about/index.jsp" class="dropdown-menu-link">About</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/support/index.jsp" class="dropdown-menu-link">Support</a></FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="/opennms/account/selfService/" v-bind="attrs" v-on="on">
            {{ username }}
            <FeatherIcon :icon="ArrowDropDown" />
          </FeatherButton>
        </template>
        <FeatherDropdownItem><a href="/opennms/account/selfService/newPasswordEntry" class="dropdown-menu-link">Change Password</a></FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/j_spring_security_logout" class="dropdown-menu-link">Log Out</a></FeatherDropdownItem>
      </FeatherDropdown>

      <FeatherDropdown class="menubar-dropdown">
        <template v-slot:trigger="{ attrs, on }">
          <FeatherButton link href="/opennms/account/selfService/" v-bind="attrs" v-on="on">{{ noticesCountUser }} {{ noticesCountOther }}</FeatherButton>
        </template>
        <FeatherDropdownItem>
          <a href="/opennms/notification/browse?acktype=unack&filter=user=={{ username }}" class="dropdown-menu-link">
            {{ noticesCountUser }} notices assigned to you</a>
        </FeatherDropdownItem>
        <FeatherDropdownItem>
          <a href="/opennms/notification/browse?acktype=unack" class="dropdown-menu-link">
            {{ noticesCountOther }} of {{ noticesCountOther }} assigned to anyone but you</a>
        </FeatherDropdownItem>
        <FeatherDropdownItem><a href="/opennms/roles" class="dropdown-menu-link">On-Call Schedule</a></FeatherDropdownItem>
      </FeatherDropdown>

      <a href="/opennms/admin/ng-requisitions/quick-add-node.jsp" class="menu-link">
        <FeatherIcon
          :icon="AddCircleAlt"
          class="pointer light-dark"
        />
      </a>

      <a href="/opennms/admin/index" class="menu-link">
        <font-awesome-icon
          icon="fa-solid fa-cogs"
          class="menu-link"
          title="Configure OpenNMS"
        ></font-awesome-icon>
      </a>

      <!--
      <FeatherButton @click="returnHandler" class="return-btn"
        >Back to main page</FeatherButton
      >
      -->
      <FeatherIcon
        :icon="LightDarkMode"
        title="Quick-Add Node"
        class="pointer light-dark"
        @click="toggleDarkLightMode(null)"
      />
    </template>
  </FeatherAppBar>
</template>

<script setup lang="ts">
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherListHeader, FeatherListItem } from '@featherds/list'
import { FeatherMegaMenu } from '@featherds/megamenu'
import { FeatherIcon } from '@featherds/icon'
import AddCircleAlt from '@featherds/icon/action/AddCircleAlt'
import ArrowDropDown from '@featherds/icon/navigation/ArrowDropDown'
import LightDarkMode from '@featherds/icon/action/LightDarkMode'
import Logo from '@/assets/Logo.vue'
import Search from './Search.vue'
import { useStore } from 'vuex'

const store = useStore()
const route = useRoute()
const returnHandler = () => window.location.href = '/opennms/'
const logo = Logo
const theme = ref('')
const light = 'open-light'
const dark = 'open-dark'
// TODO: use username from store
const username = ref('admin1')
const noticesCountUser = ref(0)
const noticesCountOther = ref(1)

const toggleDarkLightMode = (savedTheme: string | null) => {
  const el = document.body
  const newTheme = theme.value === light ? dark : light

  if (savedTheme && (savedTheme === light || savedTheme === dark)) {
    theme.value = savedTheme
    el.classList.add(savedTheme)
    return
  }

  // set the new theme on the body
  el.classList.add(newTheme)

  // remove the current theme
  if (theme.value) {
    el.classList.remove(theme.value)
  }

  // save the new theme in data and localStorage
  theme.value = newTheme
  localStorage.setItem('theme', theme.value)
  store.dispatch('appModule/setTheme', theme.value)
}
onMounted(async () => {
  const savedTheme = localStorage.getItem('theme')
  toggleDarkLightMode(savedTheme)
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
@import "@featherds/dropdown/scss/mixins";
.return-btn {
  background: var($secondary-variant);
  color: var($primary-text-on-color) !important;
  margin-right: 20px;
}
.alarm-error {
  color: var($error);
}
.dropdown-menu-link {
  color: var($primary-text-on-surface) !important;
}
.menu-link {
  color: var($primary-text-on-color) !important;
  margin-left: 2px;
}
.menubar-dropdown {
  margin-left: 2px;

  :deep(.feather-dropdown)  {
    @include dropdown-menu-height(10);
  }
}
</style>

<style lang="scss">
@import "@featherds/styles/themes/open-mixins";
body {
  background: var($background);
}
.open-light {
  @include open-light;
}
.open-dark {
  @include open-dark;
}
.light-dark {
  font-size: 24px;
  margin-top: 2px;
}
</style>
