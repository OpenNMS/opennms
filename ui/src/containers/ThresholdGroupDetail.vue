<template>
  <div class="threshold-group-detail">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <template v-if="store.currentGroup">
      <ThresholdGroupHeader
        :group="store.currentGroup"
        @edit="isEditDrawerVisible = true"
        @delete="isDeleteConfirmVisible = true"
      />

      <div class="tables">
        <ThresholdDefinitionTable
          :kind="ThresholdDefinitionKind.Threshold"
          :readOnly="!!store.currentGroup.readOnly"
        />
        <ThresholdDefinitionTable
          :kind="ThresholdDefinitionKind.Expression"
          :readOnly="!!store.currentGroup.readOnly"
        />
      </div>

      <ThresholdDefinitionDrawer />

      <ThresholdGroupDrawer
        :visible="isEditDrawerVisible"
        :isCreate="false"
        :group="store.currentGroup"
        :existingNames="store.groupNames"
        @cancel="isEditDrawerVisible = false"
        @save="onSaveGroup"
      />

      <OnmsConfirmationDialog
        v-if="isDeleteConfirmVisible"
        :visible="true"
        title="Delete this threshold group?"
        actionButtonText="Delete"
        data-test="threshold-group-detail-delete-confirm"
        @ok="onDeleteGroup"
        @cancel="isDeleteConfirmVisible = false"
      >
        <template #content>
          <p>'{{ groupName }}' and all of its thresholds will be removed.</p>
        </template>
      </OnmsConfirmationDialog>
    </template>

    <EmptyList
      v-else-if="!store.isLoading"
      :content="{ msg: `No threshold group named '${groupName}' exists.` }"
      data-test="threshold-group-not-found"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import ThresholdDefinitionDrawer from '@/components/ThresholdConfiguration/Group/ThresholdDefinitionDrawer.vue'
import ThresholdDefinitionTable from '@/components/ThresholdConfiguration/Group/ThresholdDefinitionTable.vue'
import ThresholdGroupDrawer from '@/components/ThresholdConfiguration/ThresholdGroupDrawer.vue'
import ThresholdGroupHeader from '@/components/ThresholdConfiguration/Group/ThresholdGroupHeader.vue'
import useSnackbar from '@/composables/useSnackbar'
import { ThresholdDefinitionKind } from '@/lib/thresholdValidator'
import { useMenuStore } from '@/stores/menuStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { BreadCrumb } from '@/types'
import type { ThresholdGroup } from '@/types/thresholdConfig'
import { OnmsConfirmationDialog } from '@opennms/onms-ui'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const isEditDrawerVisible = ref(false)
const isDeleteConfirmVisible = ref(false)

const groupName = computed(() => decodeURIComponent(String(route.params.name ?? '')))
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Threshold Configuration', to: '/threshold-config' },
  { label: groupName.value, to: '#', position: 'last' }
]))

const load = async () => {
  const [group] = await Promise.all([store.fetchGroup(groupName.value), store.fetchGroups()])

  store.fetchMetadata()

  if (!group.success) {
    showSnackBar({ msg: group.message, error: true })
  }
}

onMounted(load)

watch(groupName, load)

const onSaveGroup = async (group: ThresholdGroup) => {
  isEditDrawerVisible.value = false

  const previousName = groupName.value
  // The drawer only edits name and rrdRepository; keep the definitions the detail page already holds.
  const payload: ThresholdGroup = {
    ...(store.currentGroup as ThresholdGroup),
    name: group.name,
    rrdRepository: group.rrdRepository
  }

  const result = await store.renameGroup(previousName, payload)

  showSnackBar({ msg: result.success ? 'Threshold group saved.' : result.message, error: !result.success })

  if (result.success && group.name !== previousName) {
    router.replace(`/threshold-config/group/${encodeURIComponent(group.name)}`)
  } else if (result.success) {
    await store.fetchGroup(previousName)
  }
}

const onDeleteGroup = async () => {
  isDeleteConfirmVisible.value = false

  const result = await store.deleteGroup(groupName.value, store.currentGroup?.version)

  showSnackBar({ msg: result.success ? 'Threshold group deleted.' : result.message, error: !result.success })

  if (result.success) {
    router.push('/threshold-config')
  }
}
</script>

<style lang="scss" scoped>
.threshold-group-detail {
  padding: 1.5em;

  .tables {
    display: flex;
    flex-direction: column;
    gap: 1.5em;
  }
}
</style>
