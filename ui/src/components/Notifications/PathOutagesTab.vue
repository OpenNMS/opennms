<template>
  <div class="path-outages-tab">
    <div class="intro-row">
      <p class="intro">
        Define a critical path for a group of nodes so that node-down notifications are suppressed
        when the critical path is unreachable.
      </p>
      <OnmsIconButton
        variant="text"
        :icon="InfoIcon"
        tooltip="About path outages"
        aria-label="About path outages"
        data-test="path-outages-help-button"
        @click="showHelp = true"
      />
    </div>

    <div class="form-row">
      <FormField
        label="Critical Path IP Address"
        for="path-outage-ip"
        :error="!criticalIpValid ? 'Enter a valid IPv4 or IPv6 address, or leave blank to clear the path.' : undefined"
      >
        <OnmsInputText
          id="path-outage-ip"
          v-model="criticalIp"
          :invalid="!criticalIpValid"
          data-test="critical-ip-input"
        />
      </FormField>
      <FormField
        label="Critical Path Service"
        for="path-outage-service"
      >
        <OnmsSelect
          v-model="criticalSvc"
          inputId="path-outage-service"
          :options="serviceOptions"
          data-test="critical-service-select"
        />
      </FormField>
      <FormField
        class="rule-field"
        label="Node Filter Rule"
        for="path-outage-rule"
        :error="ruleError ? 'This rule could not be evaluated — check the syntax (e.g. IPADDR IPLIKE *.*.*.*).' : undefined"
      >
        <template #label-suffix>
          <HelpBadge :content="ruleHelp" ariaLabel="Node Filter Rule help" />
        </template>
        <OnmsInputText
          id="path-outage-rule"
          v-model="rule"
          :invalid="ruleError"
          data-test="rule-input"
        />
      </FormField>
    </div>
    <div class="form-actions">
      <OnmsButton
        variant="outlined"
        label="Preview Matching Nodes"
        data-test="preview-button"
        :disabled="!rule.trim()"
        @click="preview"
      />
      <OnmsButton
        :label="criticalIp.trim() ? 'Apply Critical Path' : 'Clear Critical Path'"
        :severity="criticalIp.trim() ? undefined : 'danger'"
        data-test="apply-button"
        :disabled="!rule.trim() || !criticalIpValid"
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
        <OnmsChip
          v-for="node in previewResult.nodes"
          :key="node.nodeId"
          :label="`${node.nodeLabel ?? node.nodeId}`"
        />
      </div>
    </div>

    <div class="current-paths">
      <div class="section-title">Current Critical Paths</div>
      <OnmsTable
        v-if="store.pathOutages.length"
        :value="store.pathOutages"
        dataKey="nodeId"
        paginator
        :rows="10"
        :rowsPerPageOptions="[10, 20, 50]"
        class="data-table"
        data-test="path-outages-table"
      >
        <OnmsColumn
          field="nodeLabel"
          header="Node"
          sortable
        >
          <template #body="{ data }">
            {{ data.nodeLabel ?? data.nodeId }}
          </template>
        </OnmsColumn>
        <OnmsColumn
          field="criticalPathIp"
          header="Critical Path IP"
          sortable
        />
        <OnmsColumn
          field="criticalPathServiceName"
          header="Service"
        />
        <OnmsColumn header="Actions">
          <template #body="{ data }">
            <OnmsIconButton
              :icon="DeleteIcon"
              tooltip="Remove"
              :aria-label="`Remove critical path for ${data.nodeLabel ?? data.nodeId}`"
              data-test="remove-path-outage-button"
              @click="askRemove(data)"
            />
          </template>
        </OnmsColumn>
      </OnmsTable>
      <div v-if="!store.pathOutages.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </div>

  <OnmsMessageDialog
    :visible="showHelp"
    :relative="true"
    maxWidth="50em"
    maxHeight="80vh"
    title="Path Outages"
    data-test="path-outages-help"
    @close="showHelp = false"
  >
    <template #content>
      <div class="help-content">
        <p>
          Define a critical path for a group of nodes so that node-down notifications are suppressed
          when the critical path is unreachable. The rule selects the nodes; leaving the IP address
          blank clears the critical path for the matching nodes.
        </p>
        <div class="help-heading">About Critical Paths and Filter Rules</div>
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
    </template>
  </OnmsMessageDialog>
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
        Clear the critical path for every node matching <code>{{ rule }}</code> that currently has one?
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
import { computed, ref, watch } from 'vue'

import { OnmsConfirmationDialog, OnmsButton, OnmsChip, OnmsColumn, OnmsIconButton, OnmsInputText, OnmsMessageDialog, OnmsSelect, OnmsTable } from '@opennms/onms-ui'

import EmptyList from '@/components/Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import HelpBadge from '@/components/Common/HelpBadge.vue'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { PathOutage, PathOutagePreview } from '@/types/notificationConfig'

const store = useNotificationConfigStore()
const { withSpinner, report } = useActionFeedback()

// ICMP is the only supported critical path service (matches the legacy wizard).
const serviceOptions = ['ICMP']

const showHelp = ref(false)
const criticalIp = ref('')
const criticalSvc = ref('ICMP')
const rule = ref('')
const previewResult = ref<PathOutagePreview | null>(null)

// A backend rule evaluation that returns nothing marks the rule field invalid;
// editing the rule clears it so the red state doesn't linger.
const ruleError = ref(false)
watch(rule, () => {
  ruleError.value = false
})

const ruleHelp = 'An OpenNMS filter rule selecting the nodes, e.g. IPADDR IPLIKE *.*.*.*. Octets accept * (any), ranges (0-3) and lists (0,1,2). Use Preview to check what it matches.'

// Client-side format check only; the server is authoritative when the path is applied.
const isValidIp = (value: string): boolean => {
  const isV4 = (v: string): boolean => {
    const m = v.match(/^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/)
    return !!m && m.slice(1).every(octet => Number(octet) <= 255)
  }
  if (isV4(value)) {
    return true
  }
  // IPv6: at most one "::"; between colons only hex groups (1-4 digits), with an
  // optional embedded IPv4 tail (e.g. ::ffff:192.168.1.1). Rejects colon-only junk.
  if (!value.includes(':') || (value.match(/::/g) || []).length > 1) {
    return false
  }
  let body = value
  const lastColon = value.lastIndexOf(':')
  const tail = value.slice(lastColon + 1)
  if (tail.includes('.')) {
    if (!isV4(tail)) {
      return false
    }
    body = value.slice(0, lastColon + 1)
  }
  return body.replace('::', ':').split(':').every(group => group === '' || /^[0-9a-fA-F]{1,4}$/.test(group))
}
const criticalIpValid = computed(() => {
  const v = criticalIp.value.trim()
  return v === '' || isValidIp(v)
})

const showApplyConfirmation = ref(false)
const applyCount = ref<number | null>(null)
const showRemoveConfirmation = ref(false)
const outageToRemove = ref<PathOutage | null>(null)

const emptyListContent = {
  msg: 'No critical paths configured.'
}

// A rule the server can't evaluate marks the rule field invalid as well as
// showing the server's reason.
const runPreview = async (): Promise<PathOutagePreview | null> => {
  const result = await withSpinner(() => store.previewPathOutageRule(rule.value.trim()))
  ruleError.value = !report(result)
  previewResult.value = result.payload ?? null
  return previewResult.value
}

const preview = async () => {
  await runPreview()
}

const askApply = async () => {
  const result = await runPreview()
  if (!result) {
    return
  }
  // applying: every node matching the rule gets the critical path. The clear case
  // affects an unknown subset (only nodes that already have a path) and the server
  // caps the preview node list, so its confirmation shows no count.
  applyCount.value = result.totalCount
  showApplyConfirmation.value = true
}

const confirmApply = async () => {
  showApplyConfirmation.value = false
  const criticalPathIp = criticalIp.value.trim() || undefined
  const result = await withSpinner(() => store.applyPathOutage({
    rule: rule.value.trim(),
    criticalIp: criticalPathIp,
    criticalSvc: criticalSvc.value
  }))
  if (report(result, criticalPathIp ? 'Critical path applied.' : 'Critical path cleared.')) {
    previewResult.value = null
  }
}

const askRemove = (outage: PathOutage) => {
  outageToRemove.value = outage
  showRemoveConfirmation.value = true
}

const confirmRemove = async () => {
  if (outageToRemove.value) {
    const nodeId = outageToRemove.value.nodeId
    report(await withSpinner(() => store.deletePathOutage(nodeId)), 'Critical path removed.')
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

.intro-row {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-bottom: 1rem;
  max-width: 1200px;
}

.intro {
  margin: 0;
  max-width: 80ch;
  color: var(--p-text-muted-color);
}

// Help dialog body (slot content, so this component's scoped styles apply).
// One column; OnmsMessageDialog scrolls it past maxHeight.
.help-content {
  .help-heading {
    font-size: 1rem;
    font-weight: 600;
    margin: 1rem 0 0.75rem 0;
  }

  .help-section {
    margin-bottom: 0.5rem;
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

// Cap the form so its fields and action buttons stay grouped together instead
// of the rule input stretching and pushing the buttons off toward the edge.
.form-row {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  flex-wrap: wrap;
  max-width: 1200px;

  :deep(.p-select) {
    min-width: 200px;
  }

  .rule-field {
    flex: 1;
    min-width: 240px;
    max-width: 420px;

    :deep(input) {
      width: 100%;
    }
  }
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-top: 1rem;
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
