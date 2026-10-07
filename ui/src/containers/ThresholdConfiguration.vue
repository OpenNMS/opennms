<template>
  <div class="threshold-config-container">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Threshold Configuration</h1>
      </div>
    </div>
    <div class="tab-container">
      <ThresholdConfigTabContainer />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import ThresholdConfigTabContainer from '@/components/ThresholdConfiguration/ThresholdConfigTabContainer.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const groupStore = useThresholdGroupStore()
const threshdStore = useThreshdConfigurationStore()
const { showSnackBar } = useSnackbar()

const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Threshold Configuration', to: '#', position: 'last' }
]))

onMounted(async () => {
  // The two configurations are independent, so one failing should not hide the other.
  const [groups, threshd] = await Promise.all([
    groupStore.fetchGroups(),
    threshdStore.fetchConfiguration()
  ])

  groupStore.fetchMetadata()

  const failure = [groups, threshd].find(result => !result.success)

  if (failure) {
    showSnackBar({ msg: failure.message, error: true })
  }
})
</script>

<style lang="scss" scoped>
.threshold-config-container {
  padding: 1.5em;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.25em;
    padding: 0;

    h1 {
      margin: 0;
    }
  }

  .tab-container {
    padding: 0;
  }
}
</style>
