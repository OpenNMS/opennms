<template>
  <div class="main-content">
    <div class="header">
      <div>
        <FeatherBackButton
          data-test="back-button"
          @click="onDetailsCancel"
        >
          Go Back
        </FeatherBackButton>
      </div>
      <div>
        <h3>
          {{ isCreate ? 'Create New SNMP Definition' : 'Edit SNMP Definition Details' }}
        </h3>
      </div>
      <FeatherIcon
        :icon="InfoIcon"
        class="info-icon"
        @click="isMessageDialogVisible = true"
        data-test="snmp-config-lookup-info-icon"
      />
    </div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div class="section-content">
        <h4>Configuration applies to these IP Ranges:</h4>
        <FeatherChipList label="IP Addresses" v-if="badgeItems.length">
          <FeatherChip
            v-for="item of badgeItems"
            :key="createBadgeKey(item)"
            class="definition-chip"
            @click="removeChip(item)"
          >
            <template v-if="item.type === 'range'">{{ `${item.range?.begin} - ${item.range?.end}` }}</template>
            <template v-else-if="item.type === 'specific'">{{ item.specific }}</template>
            <template v-else>IPLIKE: {{ item.ipMatch }}</template>

            <template #icon>
              <FeatherIcon
                :icon="IconCancel"
                class="icon"
              />
            </template>
          </FeatherChip>
        </FeatherChipList>

        <div v-if="currentDefinition?.profileLabel">
          <div class="large-spacer"></div>
          <label class="label">Profile Label: </label>
          {{ currentDefinition?.profileLabel }}
          <div class="large-spacer"></div>
        </div>

        <SnmpConfigDetailsPanel
          ref="detailsPanelRef"
          v-if="snmpAgentConfig"
          :displayIps="true"
          :isCreate="isCreate"
          :firstIp="firstIpAddress"
          :lastIp="lastIpAddress"
          :ipMatch="ipMatchValue"
          :config="snmpAgentConfig"
          :errors="errors"
          @add-range="onAddRange"
          @cancel="onDetailsCancel"
          @validation-error="onDetailsValidationError"
          @save="onDetailsSave"
        />
      </div>
    </div>
    <MessageDialog
      :visible="isMessageDialogVisible"
      maxHeight="22em"
      maxWidth="50em"
      title="SNMP Definition Details"
      @close="isMessageDialogVisible = false"
    >
      <template #content>
        <div class="snmp-definition-message-dialog-help-body">
          <p>The SNMP Configuration UI allows you to manage SNMP definitions. SNMP definitions are used to specify the SNMP settings that should be applied to nodes based on their IP address or other criteria.</p>

          <p>The top section, <strong>Configuration applies to these IP Ranges</strong>, shows badges with each specific IP, IP range or IPLIKE expression that is being viewed or edited.</p>

          <p><strong>NOTE:</strong> The same detail view is used whether you arrived via <strong>Lookup by IP</strong> or via <strong>Manage Definitions</strong>.</p>

          <p>In the case of a lookup, there will be only one badge for the specific IP address that was looked up. In the case of editing, either on this screen or the <strong>Manage Definitions</strong> screen, there may be multiple badges for the various IP addresses, IP ranges or IPLIKE expressions that are being edited.</p>

          <p>In the input fields below, you can add a single IP address, specify a first and last IP address of a range, or specify an IPLIKE expression. Then click <strong>Add</strong>, and a new badge will appear in the top section.</p>

          <p>Click the <strong>X</strong> on a badge to remove that item from the configuration being edited.</p>

          <p>You may then edit all the other parameters as needed, and click <strong>Save</strong> to save the configuration, which will now apply to all the IP addresses, ranges or IPLIKE expressions specified in the badges.</p>

          <p><strong>NOTE:</strong> OpenNMS will optimize the application of these configurations to the specified IP addresses, ranges, or IPLIKE expressions, for
          example by consolidating overlapping ranges.</p>

          <h4>Secure Credentials Vault (SCV) Support</h4>

          <p>For secure parameters such as read/write community strings or SNMP v3 authentication and privacy credentials, you can choose to store these in the OpenNMS Secure Credentials Vault. This allows you to keep sensitive information secure while still allowing OpenNMS to access it when needed.</p>

          <p>Click on the SCV icon next to those fields to open the SCV drawer which allows you to select a credential from the vault. This will enter a reference to that credential in the field, which OpenNMS will resolve to the actual credential value when it needs to use it. A typical expression looks like <code>${scv:snmp-read-community}</code>, where <code>snmp-read-community</code> is the name of the credential stored in the vault. Note, you can also enter the reference expression directly into the field without using the SCV drawer, as long as you use the correct syntax.</p>
        </div>
      </template>
    </MessageDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

import { FeatherBackButton } from '@featherds/back-button'
import { FeatherChip, FeatherChipList } from '@featherds/chips'
import { FeatherIcon } from '@featherds/icon'
import InfoIcon from '@featherds/icon/action/Info'
import IconCancel from '@featherds/icon/navigation/Cancel'
import MessageDialog from '../Common/MessageDialog.vue'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'
import { convertSnmpVersionToString } from '@/services/snmpConfigService'
import { getDefaultSnmpDefinition } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpDefinition, SnmpConfigFormErrors, IpAddressRange } from '@/types/snmpConfig'
import SnmpConfigDetailsPanel from './SnmpConfigDetailsPanel.vue'
import { checkForDuplicateDefinitionItems } from '@/lib/snmpValidator'

const props = defineProps<{
  isCreate: boolean,
  definition: SnmpDefinition | null
}>()

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', definition: SnmpDefinition): void
  (e: 'validation-error', errors: SnmpConfigFormErrors): void
}>()

interface DefinitionBadgeItem {
  type: 'range' | 'specific' | 'ipmatch'
  range?: IpAddressRange
  specific?: string
  ipMatch?: string
}

const isMessageDialogVisible = ref(false)
const isValid = ref(false)
const errors = ref<SnmpConfigFormErrors>({})
const currentDefinition = ref<SnmpDefinition>(getDefaultSnmpDefinition())
const firstIpAddress = ref('')
const lastIpAddress = ref('')
const ipMatchValue = ref('')
const detailsPanelRef = ref<InstanceType<typeof SnmpConfigDetailsPanel> | null>(null)

const snmpAgentConfig = computed(() => {
  const config = {
    version: convertSnmpVersionToString(currentDefinition.value?.version ?? 'v2c'),
    location: currentDefinition.value?.location ?? DEFAULT_MONITORING_LOCATION,
    readCommunity: currentDefinition.value?.readCommunity ?? '',
    writeCommunity: currentDefinition.value?.writeCommunity ?? '',
    timeout: currentDefinition.value?.timeout ?? undefined,
    retry: currentDefinition.value?.retry ?? undefined,
    port: currentDefinition.value?.port ?? undefined,
    proxyHost: currentDefinition.value?.proxyHost ?? '',
    maxRequestSize: currentDefinition.value?.maxRequestSize ?? undefined,
    maxVarsPerPdu: currentDefinition.value?.maxVarsPerPdu ?? undefined,
    maxRepetitions: currentDefinition.value?.maxRepetitions ?? undefined,
    ttl: currentDefinition.value?.ttl ?? undefined,
    securityName: currentDefinition.value?.securityName ?? '',
    securityLevel: currentDefinition.value?.securityLevel ?? undefined,
    authPassphrase: currentDefinition.value?.authPassphrase ?? '',
    authProtocol: currentDefinition.value?.authProtocol ?? '',
    engineId: currentDefinition.value?.engineId ?? '',
    contextEngineId: currentDefinition.value?.contextEngineId ?? '',
    contextName: currentDefinition.value?.contextName ?? '',
    privacyPassphrase: currentDefinition.value?.privacyPassphrase ?? '',
    privacyProtocol: currentDefinition.value?.privacyProtocol ?? '',
    enterpriseId: currentDefinition.value?.enterpriseId ?? ''
  } as SnmpAgentConfig

  return config
})

const resetValues = () => {
  clearIpInputs()
  currentDefinition.value = getDefaultSnmpDefinition()
}

const clearIpInputs = () => {
  firstIpAddress.value = ''
  lastIpAddress.value = ''
  ipMatchValue.value = ''
}

const createBadgeKey = (badge: DefinitionBadgeItem) => {
  if (badge.type === 'range') {
    return `range-${badge.range?.begin}-${badge.range?.end}`
  } else if (badge.type === 'specific') {
    return `specific-${badge.specific}`
  }
  return `ipmatch-${badge.ipMatch}`
}

const badgeItems = computed<DefinitionBadgeItem[]>(() => {
  const items: DefinitionBadgeItem[] = []

  if (currentDefinition.value?.range?.length) {
    for (const range of currentDefinition.value.range) {
      items.push({ type: 'range', range })
    }
  }

  if (currentDefinition.value?.specific?.length) {
    for (const specific of currentDefinition.value.specific) {
      items.push({ type: 'specific', specific })
    }
  }

  if (currentDefinition.value?.ipMatch?.length) {
    for (const ipMatch of currentDefinition.value.ipMatch) {
      items.push({ type: 'ipmatch', ipMatch: ipMatch })
    }
  }

  return items
})

const removeChip = (item: DefinitionBadgeItem) => {
  if (!currentDefinition.value) {
    return
  }

  if (item.type === 'range' && item.range && currentDefinition.value.range) {
    currentDefinition.value.range = currentDefinition.value.range.filter(
      r => !(r.begin === item.range!.begin && r.end === item.range!.end)
    )
  } else if (item.type === 'specific' && item.specific && currentDefinition.value.specific) {
    currentDefinition.value.specific = currentDefinition.value.specific.filter(
      s => s !== item.specific
    )
  } else if (item.type === 'ipmatch' && item.ipMatch && currentDefinition.value.ipMatch) {
    currentDefinition.value.ipMatch = currentDefinition.value.ipMatch.filter(
      m => m !== item.ipMatch
    )
  }
}

const onAddRange = (firstIp: string, lastIp: string, ipMatch: string) => {
  // should not occur since the add range button in SnmpConfigDetailsPanel is disabled unless at least one of these fields has a value, but guard against it just in case
  if (!firstIp && !lastIp && !ipMatch) {
    errors.value = {
      invalidRangeConfig: 'Cannot add an empty range.'
    }

    emit('validation-error', errors.value)
    return
  }

  // should not occur since SnmpConfigDetailsPanel does not allow entering an ipMatch if there are already range/specific values (or vice versa), but guard against it just in case
  if (ipMatch && (firstIp || lastIp)) {
    errors.value = {
      mixingRangeWithIpMatch: 'You cannot mix range/specific with IP Match expressions in the same definition.'
    }

    emit('validation-error', errors.value)
    return
  }

  // SnmpConfigDetailsPanel already handles trying to add a range/specific if the definition input fields already have an ipMatch (or vice-versa).
  // Here we handle trying to add an ipMatch when the existing definition already has ranges/specifics (or vice versa), which is also not allowed.
  const currentHasRangesOrSpecifics = Boolean(currentDefinition.value?.range?.length || currentDefinition.value?.specific?.length)
  const currentHasIpMatch = Boolean(currentDefinition.value?.ipMatch?.length)
  const isAddingIpMatch = Boolean(ipMatch)
  const isAddingRangeOrSpecific = Boolean(firstIp || lastIp)

  if ((isAddingIpMatch && currentHasRangesOrSpecifics) || (isAddingRangeOrSpecific && currentHasIpMatch)) {
    errors.value = {
      mixingRangeWithIpMatch: 'You cannot mix range/specific with IP Match expressions in the same definition.'
    }

    emit('validation-error', errors.value)
    return
  }

  if (firstIp && lastIp) {
    const newRange = { begin: firstIp, end: lastIp } as IpAddressRange

    const duplicateError = checkForDuplicateDefinitionItems(currentDefinition.value, newRange, undefined, undefined)

    if (duplicateError) {
      errors.value = {
        duplicateRangeItem: duplicateError
      }

      emit('validation-error', errors.value)
      return
    }

    if (currentDefinition.value.range) {
      currentDefinition.value.range = [
        ...currentDefinition.value.range,
        newRange
      ]
    } else {
      currentDefinition.value.range = [newRange]
    }
  }

  if (firstIp && !lastIp) {
    const duplicateError = checkForDuplicateDefinitionItems(currentDefinition.value, undefined, firstIp, undefined)

    if (duplicateError) {
      errors.value = {
        duplicateRangeItem: duplicateError
      }

      emit('validation-error', errors.value)
      return
    }

    if (currentDefinition.value.specific) {
      currentDefinition.value.specific = [
        ...currentDefinition.value.specific,
        firstIp
      ]
    } else {
      currentDefinition.value.specific = [firstIp]
    }
  }

  if (ipMatch) {
    const duplicateError = checkForDuplicateDefinitionItems(currentDefinition.value, undefined, undefined, ipMatch)

    if (duplicateError) {
      errors.value = {
        duplicateRangeItem: duplicateError
      }

      emit('validation-error', errors.value)
      return
    }

    if (currentDefinition.value.ipMatch) {
      currentDefinition.value.ipMatch = [
        ...currentDefinition.value.ipMatch,
        ipMatch
      ]
    } else {
      currentDefinition.value.ipMatch = [ipMatch]
    }
  }

  clearIpInputs()

  // Clear the child component's input fields as well
  if (detailsPanelRef.value) {
    detailsPanelRef.value.clearIpFields()
  }
}

const loadInitialValues = () => {
  if (!props.definition) {
    currentDefinition.value = getDefaultSnmpDefinition()
  } else {
    currentDefinition.value = props.definition
  }

  clearIpInputs()
}

const onDetailsValidationError = (formErrors: SnmpConfigFormErrors) => {
  isValid.value = Object.keys(formErrors).length === 0
  errors.value = { ...formErrors }
}

const onDetailsSave = async (config: SnmpAgentConfig, firstIp?: string, lastIp?: string, ipMatch?: string) => {
  firstIpAddress.value = firstIp ?? ''
  lastIpAddress.value = lastIp ?? ''
  ipMatchValue.value = ipMatch ?? ''

  const definitionToSave = {
    ...currentDefinition.value,
    ipMatch: currentDefinition.value?.ipMatch ?? [],
    location: config.location,
    port: config.port,
    retry: config.retry,
    timeout: config.timeout,
    readCommunity: config.readCommunity,
    writeCommunity: config.writeCommunity,
    proxyHost: config.proxyHost,
    version: config.version,
    maxVarsPerPdu: config.maxVarsPerPdu,
    maxRepetitions: config.maxRepetitions,
    maxRequestSize: config.maxRequestSize,
    securityName: config.securityName,
    securityLevel: config.securityLevel,
    authPassphrase: config.authPassphrase,
    authProtocol: config.authProtocol,
    privacyPassphrase: config.privacyPassphrase,
    privacyProtocol: config.privacyProtocol,
    engineId: config.engineId,
    contextEngineId: config.contextEngineId,
    contextName: config.contextName,
    enterpriseId: config.enterpriseId,
    ttl: config.ttl
  } as SnmpDefinition

  emit('save', definitionToSave)
}

const onDetailsCancel = () => {
  resetValues()
  emit('cancel')
}

watch([() => props.definition, () => props.isCreate], () => {
  loadInitialValues()
})

onMounted(() => {
  loadInitialValues()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables' as variables;
@use '@featherds/styles/mixins/typography';
@use "@featherds/table/scss/table";


.snmp-definition-message-dialog-help-body {
  p {
    margin: 0.5em 0;
  }

  h4 {
    margin-top: 1em;
  }

  code {
    background-color: var(variables.$background);
    padding: 0.2em 0.4em;
    border-radius: 4px;
    font-family: monospace;
  }
}

.main-content {
  padding: 0.2em;
  margin: 0.2em;
  border-radius: 8px;
  background-color: var(variables.$surface);

  .header {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .info-icon {
    cursor: pointer;
    font-size: 1.5em;
    margin-left: 0.5em;
    color: var(variables.$primary);

    &:hover {
      opacity: 0.8;
    }
  }

  .basic-info {
    border-width: 1px;
    border-style: solid;
    border-color: var(variables.$border-on-surface);
    padding: 1em;
    border-radius: 8px;

    .label {
      font-weight: 600;
    }

    .section-content {
      width: 80%;
    }
  }

  .large-spacer {
    min-height: 1em;
  }

  .spacer {
    min-height: 0.5em;
  }

  .definition-chip {
    background-color: var(--feather-border-on-surface);
  }
}
</style>
