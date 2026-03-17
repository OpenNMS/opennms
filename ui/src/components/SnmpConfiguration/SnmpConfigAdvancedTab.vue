<template>
  <div class="snmp-config-advanced-tab">
    <FeatherTabContainer
      class="nested-tabs"
      v-model="activeAdvancedSubtab"
    >
      <template v-slot:tabs>
        <FeatherTab>Default Overrides</FeatherTab>
        <FeatherTab>Profiles</FeatherTab>
        <FeatherTab>Upload/Download</FeatherTab>
      </template>
      <FeatherTabPanel>
        <SnmpConfigDefaultsPanel />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigProfilesTab />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <SnmpConfigUploadDownloadTab />
      </FeatherTabPanel>
    </FeatherTabContainer>
   </div>
</template>

<script lang="ts" setup>
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import SnmpConfigDefaultsPanel from './SnmpConfigDefaultsPanel.vue'
import SnmpConfigProfilesTab from './SnmpConfigProfilesTab.vue'
import SnmpConfigUploadDownloadTab from './SnmpConfigUploadDownloadTab.vue'

const store = useSnmpConfigStore()

const activeAdvancedSubtab = computed({
  get: () => store.activeAdvancedSubtab,
  set: (val) => store.setActiveAdvancedSubtab(val)
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.snmp-config-advanced-tab {
  background: var(variables.$surface);
  width: 100%;
  padding: 0;
  border-radius: 5px;
  margin-top: 0;
  border: 1px solid var(variables.$border-on-surface);

  .main-section {
    padding: 1.5em;
  }

  .nested-tabs {
    margin-top: 1em;

    :deep(li .tab) {
      height: 2.25rem;
    }
  }
}
</style>
