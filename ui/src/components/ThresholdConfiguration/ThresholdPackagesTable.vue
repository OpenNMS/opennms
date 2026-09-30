<template>
  <TableCard>
    <div class="table-header">
      <h3>Packages</h3>
      <OnmsButton data-test="threshd-package-create" @click="isDrawerVisible = true">New package</OnmsButton>
    </div>

    <OnmsTable :value="store.config.packages" data-test="threshd-packages-table">
      <OnmsColumn header="Name">
        <template #body="{ data }">
          <router-link :to="packageRoute(data.name)" data-test="threshd-package-link">{{ data.name }}</router-link>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Filter" field="filter" />
      <OnmsColumn header="Services">
        <template #body="{ data }">{{ data.services?.length || 0 }}</template>
      </OnmsColumn>
      <OnmsColumn header="Threshold groups">
        <template #body="{ data }">{{ groupsOf(data) }}</template>
      </OnmsColumn>
      <OnmsColumn header="Outage calendars">
        <template #body="{ data }">{{ data.outageCalendars?.join(', ') || '--' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsIconButton
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Delete package"
            aria-label="Delete package"
            data-test="threshd-package-delete"
            :disabled="store.config.packages.length <= 1"
            @click="pendingDelete = data.name"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!store.config.packages.length"
      :content="{ msg: 'No threshd packages configured.' }"
      data-test="threshd-packages-empty"
    />

    <p v-else-if="store.config.packages.length === 1" class="hint">
      At least one package is required, so the last one cannot be deleted.
    </p>

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this package?"
      actionButtonText="Delete"
      data-test="threshd-package-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = ''"
    >
      <template #content>
        <p>Thresholding stops for every interface '{{ pendingDelete }}' selects.</p>
      </template>
    </OnmsConfirmationDialog>

    <ThresholdPackageDrawer
      :visible="isDrawerVisible"
      :existingNames="store.config.packages.map(pkg => pkg.name)"
      @cancel="isDrawerVisible = false"
      @save="onSaveNewPackage"
    />
  </TableCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ThresholdPackageDrawer from '@/components/ThresholdConfiguration/ThresholdPackageDrawer.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLDING_GROUP_PARAMETER } from '@/lib/thresholdValidator'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import type { ThreshdPackage } from '@/types/thresholdConfig'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const store = useThreshdConfigurationStore()
const router = useRouter()
const { showSnackBar } = useSnackbar()

const pendingDelete = ref('')
const isDrawerVisible = ref(false)

const packageRoute = (name: string) => `/threshold-config/package/${encodeURIComponent(name)}`

const groupsOf = (pkg: ThreshdPackage) => {
  const groups = (pkg.services ?? [])
    .flatMap(service => service.parameters ?? [])
    .filter(parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER && parameter.value)
    .map(parameter => parameter.value)

  return groups.length ? [...new Set(groups)].join(', ') : '--'
}

const onSaveNewPackage = async (pkg: ThreshdPackage) => {
  isDrawerVisible.value = false

  const result = await store.createPackage(pkg)

  showSnackBar({ msg: result.success ? 'Threshd package created.' : result.message, error: !result.success })

  if (result.success) {
    router.push(packageRoute(pkg.name))
  }
}

const onConfirmDelete = async () => {
  const name = pendingDelete.value
  pendingDelete.value = ''

  const result = await store.deletePackage(name)

  showSnackBar({ msg: result.success ? 'Threshd package deleted.' : result.message, error: !result.success })
}
</script>

<style lang="scss" scoped>
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1em 0.75em 1em;

  h3 {
    margin: 0;
  }
}

.hint {
  color: var(--p-text-muted-color);
  padding: 0 1em;
}
</style>
