<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
      <SideMenu
        pushedSelector=".app-layout"
      />
    </template>

    <div class="main-content">
      <Spinner />
      <Snackbar />
      <WelcomeModal
        v-if="showWelcomeModal"
        :show-change-password="showChangePassword"
        :show-usage-statistics="showUsageStatistics"
        :show-community-signup="showCommunitySignup"
        @close="onWelcomeModalClose"
      />
      <router-view v-slot="{ Component }">
        <keep-alive include="MapKeepAlive">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
    <template v-slot:footer>
      <Footer />
    </template>
  </FeatherAppLayout>
</template>

<script
  setup
  lang="ts"
>
import { FeatherAppLayout } from '@featherds/app-layout'
import Footer from '@/components/Layout/Footer.vue'
import Menubar from '@/components/Menu/Menubar.vue'
import SideMenu from '@/components/Menu/SideMenu.vue'
import Spinner from '@/components/Common/Spinner.vue'
import Snackbar from '@/components/Common/Snackbar.vue'
import WelcomeModal from '@/components/WelcomeModal/WelcomeModal.vue'
import { useAuthStore } from '@/stores/authStore'
import { useInfoStore } from '@/stores/infoStore'
import { usePluginStore } from '@/stores/pluginStore'
import { useMenuStore } from '@/stores/menuStore'
import { useMonitoringSystemStore } from '@/stores/monitoringSystemStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import API from '@/services'

const authStore = useAuthStore()
const infoStore = useInfoStore()
const menuStore = useMenuStore()
const monitoringSystemStore = useMonitoringSystemStore()
const nodeStructureStore = useNodeStructureStore()
const pluginStore = usePluginStore()

const showWelcomeModal = ref(false)
const showChangePassword = ref(false)
const showUsageStatistics = ref(false)
const showCommunitySignup = ref(false)

const router = useRouter()

// Navigates to the classic OpenNMS dashboard. Derives the context path
// from the current URL so it works regardless of deployment prefix.
// e.g. '/opennms/ui/index.html' → '/opennms/index.jsp'
const redirectToDashboard = () => {
  const contextPath = window.location.pathname.replace(/\/ui\/.*$/, '')
  window.location.assign(`${contextPath}/index.jsp`)
}

const onWelcomeModalClose = () => {
  showWelcomeModal.value = false
  // After the wizard the user lands back on the blank home route — send
  // them to the real dashboard instead.
  if (router.currentRoute.value.path === '/') {
    redirectToDashboard()
  }
}

const checkFirstSignInModals = async () => {
  const roles = authStore.whoAmI.roles ?? []
  const isAdmin = roles.includes('ROLE_ADMIN')
  const isAdminOrRest = isAdmin || roles.includes('ROLE_REST')

  const [requiresPasswordChange, usageStatus, productUpdateStatus] = await Promise.all([
    isAdmin ? API.getRequiresPasswordChange() : null,
    isAdmin ? API.getUsageStatisticsStatus() : null,
    isAdminOrRest ? API.getProductUpdateEnrollmentStatus() : null
  ])

  if (requiresPasswordChange) {
    showChangePassword.value = true
  }

  if (usageStatus && !usageStatus.initialNoticeAcknowledged) {
    showUsageStatistics.value = true
  }

  if (productUpdateStatus && !productUpdateStatus.noticeAcknowledged) {
    showCommunitySignup.value = true
  }

  showWelcomeModal.value = showChangePassword.value || showUsageStatistics.value || showCommunitySignup.value

  // Nothing to show and still on the blank home route — go straight to the dashboard.
  if (!showWelcomeModal.value && router.currentRoute.value.path === '/') {
    redirectToDashboard()
  }
}

onMounted(async () => {
  await authStore.getWhoAmI()
  checkFirstSignInModals()

  infoStore.getInfo()
  menuStore.getMainMenu()
  menuStore.getNotificationSummary()
  menuStore.loadSideMenuExpanded()
  monitoringSystemStore.getMainMonitoringSystem()
  nodeStructureStore.getCategories()
  nodeStructureStore.getMonitoringLocations()
  pluginStore.getPlugins()
})
</script>

<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";
@import "@featherds/styles/themes/variables";

html {
  overflow-x: hidden;
}
.main-content {
  table {
    width: 100%;
  }
}
a {
  text-decoration: none;
  color: var($clickable-normal);
}
.pointer {
  cursor: pointer !important;
}

// global feather typography classes
.headline3 {
  @include headline3;
}
.headline4 {
  @include headline4;
}
.subtitle1 {
  @include subtitle1;
}
.subtitle2 {
  @include subtitle2;
}
</style>
