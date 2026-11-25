<template>
  <div class="resource-types-container">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <h1>Resource Types Configuration</h1>
    </div>
    <div class="intro">
      <p>
        Resource types describe the kinds of resources, such as disks or database tables, that data is
        collected for. They are kept in configurations, one per former file in etc/resource-types.d.
        <OnmsIconButton
          :icon="InfoIcon"
          iconSize="1.2em"
          class="info-icon-button"
          title="About resource types"
          aria-label="About resource types"
          data-test="resource-types-info-icon"
          @click="isHelpVisible = true"
        />
      </p>
    </div>

    <ResourceTypesConfigTable />

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="Resource types"
      :text="CONFIG_HELP"
      @close="isHelpVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import ResourceTypesConfigTable from '@/components/ResourceTypesConfiguration/ResourceTypesConfigTable.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { CONFIG_HELP } from '@/lib/resourceTypeHelpText'
import { useMenuStore } from '@/stores/menuStore'
import { useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import { BreadCrumb } from '@/types'
import { OnmsIconButton } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const menuStore = useMenuStore()
const store = useResourceTypesConfigStore()
const { report } = useActionFeedback()

const isHelpVisible = ref(false)

const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Resource Types Configuration', to: '#', position: 'last' }
]))

onMounted(async () => {
  report(await store.fetchConfigs())
})
</script>

<style lang="scss" scoped>
@use '@/components/ThresholdConfiguration/Common/info-icon-button' as *;

.info-icon-button {
  @include info-icon-button;
}

.resource-types-container {
  padding: 1.5em;

  .header {
    margin-bottom: 1.25em;

    h1 {
      margin: 0;
    }
  }

  .intro p {
    margin: 0 0 1em 0;
  }
}
</style>
