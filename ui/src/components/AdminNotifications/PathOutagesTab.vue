<template>
  <div class="path-outages-tab">
    <p class="intro">
      Define a critical path for a group of nodes so node-down notifications are suppressed when
      the critical path is unreachable. The rule selects the nodes; leaving the IP address blank
      clears the critical path for the matching nodes.
    </p>

    <TogglePanel
      :collapsed="helpCollapsed"
      class="help-panel"
      data-test="path-outages-help"
      @update:collapsed="(value: boolean) => (helpCollapsed = value)"
    >
      <template #header>
        <span class="help-header">
          <i class="pi pi-question-circle" aria-hidden="true" />
          About Critical Paths and Filter Rules
        </span>
      </template>
      <div class="help-content">
        <div class="help-section">
          <div class="section-title">Critical Path IP Address</div>
          <p>
            Enter the critical path IP address in xxx.xxx.xxx.xxx or
            xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx format. Or leave it blank to clear previously
            set paths for the nodes matching the rule. The critical path service is typically ICMP,
            and at this time ICMP is the only critical path service supported.
          </p>
        </div>
        <div class="help-section">
          <div class="section-title">Node Filter Rule</div>
          <p>
            Filtering on TCP/IP address uses a very flexible format, allowing you to separate the
            four octets (fields) of a TCP/IP address into specific searches. An asterisk (*) in
            place of any octet matches any value for that octet. Ranges are indicated by two
            numbers separated by a dash (-), and commas are used for list demarcation.
          </p>
          <p>The following examples are all valid and yield the set of addresses from 192.168.0.0 through 192.168.3.255:</p>
          <ul>
            <li><code>192.168.0-3.*</code></li>
            <li><code>192.168.0-3.0-255</code></li>
            <li><code>192.168.0,1,2,3.*</code></li>
          </ul>
          <p>
            To use a rule based on TCP/IP addresses as described above, enter
            <code>IPADDR IPLIKE *.*.*.*</code> substituting your desired address fields for
            <code>*.*.*.*</code>. Otherwise, you may enter any valid rule.
          </p>
        </div>
      </div>
    </TogglePanel>

    <div class="form-row">
      <FormField>
        <IftaLabel>
          <InputText
            id="path-outage-ip"
            v-model="criticalIp"
            data-test="critical-ip-input"
          />
          <label for="path-outage-ip">Critical Path IP Address</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <Select
            v-model="criticalSvc"
            labelId="path-outage-service"
            :options="serviceOptions"
            data-test="critical-service-select"
          />
          <label for="path-outage-service">Critical Path Service</label>
        </IftaLabel>
      </FormField>
      <FormField class="rule-field">
        <IftaLabel>
          <InputText
            id="path-outage-rule"
            v-model="rule"
            data-test="rule-input"
          />
          <label for="path-outage-rule">Node Filter Rule</label>
        </IftaLabel>
      </FormField>
      <Button
        outlined
        label="Preview Matching Nodes"
        data-test="preview-button"
        :disabled="!rule.trim()"
        @click="preview"
      />
      <Button
        :label="criticalIp.trim() ? 'Apply Critical Path' : 'Clear Critical Path'"
        :severity="criticalIp.trim() ? undefined : 'danger'"
        data-test="apply-button"
        :disabled="!rule.trim()"
        @click="askApply"
      />
    </div>

    <div
      v-if="previewResult"
      class="preview-result"
      data-test="preview-result"
    >
      <strong>{{ previewResult.totalCount }}</strong> node{{ previewResult.totalCount === 1 ? '' : 's' }} match the rule<span v-if="previewResult.totalCount > previewResult.nodes.length"> (showing first {{ previewResult.nodes.length }})</span>:
      <div class="node-chips">
        <Chip
          v-for="node in previewResult.nodes"
          :key="node.nodeId"
          :label="`${node.nodeLabel ?? node.nodeId}`"
        />
      </div>
    </div>

    <div class="current-paths">
      <div class="section-title">Current Critical Paths</div>
      <DataTable
        v-if="store.pathOutages.length"
        :value="store.pathOutages"
        dataKey="nodeId"
        paginator
        :rows="10"
        :rowsPerPageOptions="[10, 20, 50]"
        class="data-table"
        data-test="path-outages-table"
      >
        <Column
          field="nodeLabel"
          header="Node"
          sortable
        >
          <template #body="{ data }">
            {{ data.nodeLabel ?? data.nodeId }}
          </template>
        </Column>
        <Column
          field="criticalPathIp"
          header="Critical Path IP"
          sortable
        />
        <Column
          field="criticalPathServiceName"
          header="Service"
        />
        <Column header="Actions">
          <template #body="{ data }">
            <Button
              text
              label="Remove"
              severity="danger"
              :aria-label="`Remove critical path for ${data.nodeLabel ?? data.nodeId}`"
              data-test="remove-path-outage-button"
              @click="askRemove(data)"
            />
          </template>
        </Column>
      </DataTable>
      <div v-if="!store.pathOutages.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </div>

  <OnmsConfirmationDialog
    :visible="showApplyConfirmation"
    :title="criticalIp.trim() ? 'Apply Critical Path' : 'Clear Critical Path'"
    :actionButtonText="criticalIp.trim() ? 'Apply' : 'Clear'"
    @ok="confirmApply"
    @cancel="showApplyConfirmation = false"
  >
    <template #content>
      <p v-if="criticalIp.trim()">
        Set the critical path to <strong>{{ criticalIp }}</strong> ({{ criticalSvc }}) for the
        <strong>{{ applyCount ?? '?' }}</strong> node{{ applyCount === 1 ? '' : 's' }} matching
        <code>{{ rule }}</code>?
      </p>
      <p v-else>
        Clear the critical path for the <strong>{{ applyCount ?? '?' }}</strong>
        node{{ applyCount === 1 ? '' : 's' }} matching <code>{{ rule }}</code>?
      </p>
    </template>
  </OnmsConfirmationDialog>
  <OnmsConfirmationDialog
    :visible="showRemoveConfirmation"
    title="Remove Critical Path"
    actionButtonText="Remove"
    @ok="confirmRemove"
    @cancel="cancelRemove"
  >
    <template #content>
      <p>Remove the critical path for <strong>{{ outageToRemove?.nodeLabel ?? outageToRemove?.nodeId }}</strong>?</p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsConfirmationDialog } from '@opennms/onms-ui'

import Button from 'primevue/button'
import Chip from 'primevue/chip'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'

import EmptyList from '@/components/Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TogglePanel from '@/components/Common/TogglePanel.vue'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { PathOutage, PathOutagePreview } from '@/types/notificationConfig'

const store = useNotificationConfigStore()

// ICMP is the only supported critical path service (matches the legacy wizard).
const serviceOptions = ['ICMP']

const helpCollapsed = ref(true)
const criticalIp = ref('')
const criticalSvc = ref('ICMP')
const rule = ref('IPADDR IPLIKE *.*.*.*')
const previewResult = ref<PathOutagePreview | null>(null)

const showApplyConfirmation = ref(false)
const applyCount = ref<number | null>(null)
const showRemoveConfirmation = ref(false)
const outageToRemove = ref<PathOutage | null>(null)

const emptyListContent = {
  msg: 'No critical paths configured.'
}

const preview = async () => {
  previewResult.value = await store.previewPathOutageRule(rule.value.trim())
}

const askApply = async () => {
  const result = await store.previewPathOutageRule(rule.value.trim())
  if (!result) {
    return
  }
  previewResult.value = result
  applyCount.value = result.totalCount
  showApplyConfirmation.value = true
}

const confirmApply = async () => {
  showApplyConfirmation.value = false
  const ok = await store.applyPathOutage({
    rule: rule.value.trim(),
    criticalIp: criticalIp.value.trim() || undefined,
    criticalSvc: criticalSvc.value
  })
  if (ok) {
    previewResult.value = null
  }
}

const askRemove = (outage: PathOutage) => {
  outageToRemove.value = outage
  showRemoveConfirmation.value = true
}

const confirmRemove = async () => {
  if (outageToRemove.value) {
    await store.deletePathOutage(outageToRemove.value.nodeId)
  }
  showRemoveConfirmation.value = false
  outageToRemove.value = null
}

const cancelRemove = () => {
  showRemoveConfirmation.value = false
  outageToRemove.value = null
}
</script>

<style lang="scss" scoped>
.path-outages-tab {
  padding: 1rem 0;
}

.intro {
  margin: 0 0 1rem 0;
  max-width: 80ch;
  color: var(--p-text-muted-color);
}

.help-panel {
  margin-bottom: 1rem;
}

.help-header {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;

  .pi-question-circle {
    color: var(--p-primary-color);
  }
}

.help-content {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  .help-section {
    flex: 1;
    min-width: 300px;
  }

  .section-title {
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  p,
  ul {
    margin: 0 0 0.75rem 0;
    font-size: 0.9rem;
    line-height: 1.5;
  }
}

.form-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;

  :deep(.p-select) {
    min-width: 200px;
  }

  .rule-field {
    flex: 1;
    min-width: 240px;

    :deep(input) {
      width: 100%;
    }
  }
}

.preview-result {
  margin-top: 1rem;

  .node-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 0.4rem;
    margin-top: 0.5rem;
  }
}

.current-paths {
  margin-top: 1.5rem;

  .section-title {
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
  }
}
</style>
