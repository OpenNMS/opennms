<template>
  <div
    class="feather-drawer-custom-padding"
    v-if="store.definitionId >= -1"
  >
    <SnmpConfigDefinitionBasicInformation
      :isCreate="store.createEditMode === CreateEditMode.Create"
      :definitionId="store.definitionId"
    />
  </div>
  <div
    v-else
    class="not-found-container"
  >
    <p>No SNMP definition found.</p>
    <FeatherButton
      primary
      @click="goBack()"
    >
      Go Back
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import SnmpConfigDefinitionBasicInformation from './SnmpConfigDefinitionBasicInformation.vue'
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { CreateEditMode } from '@/types'

const router = useRouter()
const route = useRoute()
const store = useSnmpConfigStore()

const goBack = () => {
  router.push({ name: 'SNMP Config' })
}

onMounted(() => {
  const id = route.params.id ?? ''
  const definitionId = id === 'create' ? -1 : Number(id)

  store.definitionId = definitionId
  store.createEditMode = id === 'create' ? CreateEditMode.Create : CreateEditMode.Edit
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 25px;

  p {
    @include typography.headline3;
    margin: 0;
  }
}
</style>
