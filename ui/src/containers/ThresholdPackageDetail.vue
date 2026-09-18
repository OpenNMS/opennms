<template>
  <div class="threshd-package-detail">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <template v-if="store.currentPackage">
      <div class="header">
        <div class="left">
          <OnmsIconButton
            :icon="BackIcon"
            tooltip="Back to threshold configuration"
            aria-label="Back to threshold configuration"
            data-test="threshd-package-back"
            @click="router.push('/threshold-config?tab=packages')"
          />
          <h2 data-test="threshd-package-name">{{ packageName }}</h2>
        </div>
        <OnmsButton data-test="threshd-package-save" :disabled="hasErrors(errors)" @click="onSave">
          Save package
        </OnmsButton>
      </div>

      <div class="sections">
        <PackageBasicsForm v-model="store.currentPackage" :errors="errors" />
        <PackageAddressEditor v-model="store.currentPackage" />
        <PackageServicesTable />
      </div>
    </template>

    <EmptyList
      v-else-if="!store.isLoading"
      :content="{ msg: `No threshd package named '${packageName}' exists.` }"
      data-test="threshd-package-not-found"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import PackageAddressEditor from '@/components/ThresholdConfiguration/Package/PackageAddressEditor.vue'
import PackageBasicsForm from '@/components/ThresholdConfiguration/Package/PackageBasicsForm.vue'
import PackageServicesTable from '@/components/ThresholdConfiguration/Package/PackageServicesTable.vue'
import useSnackbar from '@/composables/useSnackbar'
import { hasErrors, validateThreshdPackage } from '@/lib/thresholdValidator'
import { useMenuStore } from '@/stores/menuStore'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { BreadCrumb } from '@/types'
import { OnmsButton, OnmsIconButton } from '@opennms/onms-ui'
import BackIcon from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useThreshdConfigurationStore()
const groupStore = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const packageName = computed(() => decodeURIComponent(String(route.params.name ?? '')))
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Threshold Configuration', to: '/threshold-config' },
  { label: packageName.value, to: '#', position: 'last' }
]))

const errors = computed(() =>
  store.currentPackage
    ? validateThreshdPackage(store.currentPackage, store.packageNames, false)
    : {}
)

const load = async () => {
  // The group names drive the threshold group picker on each service.
  const [pkg] = await Promise.all([store.fetchPackage(packageName.value), groupStore.fetchGroups()])

  if (!pkg.success) {
    showSnackBar({ msg: pkg.message, error: true })
  }
}

onMounted(load)

watch(packageName, load)

const onSave = async () => {
  const previousName = packageName.value
  const newName = store.currentPackage?.name ?? previousName

  const result = await store.renamePackage(previousName, store.currentPackage!)

  showSnackBar({ msg: result.success ? 'Threshd package saved.' : result.message, error: !result.success })

  if (result.success && newName !== previousName) {
    router.replace(`/threshold-config/package/${encodeURIComponent(newName)}`)
  } else if (result.success) {
    await store.fetchPackage(previousName)
  }
}
</script>

<style lang="scss" scoped>
.threshd-package-detail {
  padding: 1.5em;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.25em;

    .left {
      display: flex;
      align-items: center;
      gap: 0.75em;
    }

    h2 {
      margin: 0;
    }
  }

  .sections {
    display: flex;
    flex-direction: column;
    gap: 1.5em;
  }
}
</style>
