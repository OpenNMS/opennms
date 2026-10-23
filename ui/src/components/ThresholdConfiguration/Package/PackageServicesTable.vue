<template>
  <TableCard class="package-services-card">
    <div class="table-header">
      <h3>Services</h3>
      <OnmsButton data-test="package-service-create" @click="onCreate">
        New service
      </OnmsButton>
    </div>

    <OnmsTable :value="services" data-test="package-services-table">
      <OnmsColumn header="Service" field="name" />
      <OnmsColumn header="Interval (ms)" field="interval" />
      <OnmsColumn header="Status">
        <template #body="{ data }">{{ data.status || 'on' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Threshold group">
        <template #body="{ data }">{{ thresholdingGroupOf(data) }}</template>
      </OnmsColumn>
      <OnmsColumn header="User defined">
        <template #body="{ data }">{{ data.userDefined ? 'Yes' : 'No' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ index }">
          <div class="actions">
            <OnmsIconButton
              :icon="EditIcon"
              tooltip="Edit service"
              aria-label="Edit service"
              data-test="package-service-edit"
              @click="onEdit(index)"
            />
            <OnmsIconButton
              :icon="DeleteIcon"
              severity="danger"
              tooltip="Delete service"
              aria-label="Delete service"
              data-test="package-service-delete"
              @click="pendingDeleteIndex = index"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!services.length"
      :content="{ msg: 'This package thresholds nothing until it has a service.' }"
      data-test="package-services-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDeleteIndex >= 0"
      :visible="true"
      title="Delete this service?"
      actionButtonText="Delete"
      data-test="package-service-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDeleteIndex = -1"
    >
      <template #content>
        <p>Thresholding stops for this service on every interface the package selects.</p>
      </template>
    </OnmsConfirmationDialog>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useSnackbar from '@/composables/useSnackbar'
import { serviceCreatePath, serviceEditPath } from '@/lib/thresholdRoutes'
import { THRESHOLDING_GROUP_PARAMETER } from '@/lib/thresholdValidator'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import type { ThreshdService } from '@/types/thresholdConfig'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'

const store = useThreshdConfigurationStore()
const router = useRouter()
const { showSnackBar } = useSnackbar()

const pendingDeleteIndex = ref(-1)

const services = computed(() => store.currentPackage?.services ?? [])

const thresholdingGroupOf = (service: ThreshdService) =>
  service.parameters?.find(parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER)?.value || '--'

// The stored name: the package page may hold a rename that is not saved yet.
const packageName = computed(() => store.loadedPackageName ?? '')

const onCreate = () => {
  router.push(serviceCreatePath(packageName.value))
}

const onEdit = (index: number) => {
  router.push(serviceEditPath(packageName.value, index))
}

const onConfirmDelete = async () => {
  const index = pendingDeleteIndex.value
  pendingDeleteIndex.value = -1

  const result = await store.deleteService(index)

  showSnackBar({ msg: result.success ? 'Service deleted.' : result.message, error: !result.success })
}
</script>

<style lang="scss" scoped>
// Padded like the event configuration table and framed like the SNMP configuration tabs.
.package-services-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0 0.75em 0;

  h3 {
    margin: 0;
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.25em;
}
</style>
