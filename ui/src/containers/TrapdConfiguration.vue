<template>
  <div class="trap-configuration-container">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Trap Listener Configuration</h1>
      </div>
      <div class="action">
        <input type="file" accept=".xml" single @change="handleConfigurationUpload" ref="fileInput" />
        <FeatherButton secondary @click="openFileDialog">
          Upload Configuration
        </FeatherButton>
      </div>
    </div>
    <div class="tab-container">
      <FeatherTabContainer v-model="store.activeTab">
        <template v-slot:tabs>
          <FeatherTab>General Configuration</FeatherTab>
          <FeatherTab>SNMPv3 User Management</FeatherTab>
        </template>
        <FeatherTabPanel>
          <GeneralConfiguration />
        </FeatherTabPanel>
        <FeatherTabPanel>
          <SnmpV3UserManagement />
          <CreateSnmpV3User />
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>

<script setup lang="ts">
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import CreateSnmpV3User from '@/components/TrapdConfiguration/CreateSnmpV3User.vue'
import GeneralConfiguration from '@/components/TrapdConfiguration/GeneralConfiguration.vue'
import SnmpV3UserManagement from '@/components/TrapdConfiguration/SnmpV3UserManagement.vue'
import useSnackbar from '@/composables/useSnackbar'
import { validateTrapdXml } from '@/lib/trapdValidator'
import { uploadTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useMenuStore } from '@/stores/menuStore'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { BreadCrumb } from '@/types'
import { FeatherButton } from '@featherds/button'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

const menuStore = useMenuStore()
const store = useTrapdConfigStore()
const fileInput = ref<HTMLInputElement | null>(null)
const { showSnackBar } = useSnackbar()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Trap Listener Configuration', to: '#', position: 'last' }
]))

const openFileDialog = () => {
  fileInput.value?.click()
}

const handleConfigurationUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) {
    return
  }

  const file = input.files[0]

  // Reset input so the same file can be re-uploaded
  if (fileInput.value) {
    fileInput.value.value = ''
  }

  if (!file.name.endsWith('.xml')) {
    showSnackBar({ msg: 'Only .xml files are supported.', error: true })
    return
  }

  let xmlContent = ''
  try {
    xmlContent = await file.text()
  } catch {
    showSnackBar({ msg: 'Failed to read XML file.', error: true })
    return
  }

  const validationResult = validateTrapdXml(xmlContent)
  if (!validationResult.valid) {
    const errorList = validationResult.errors.slice(0, 3).map((error) => error.message).join(' | ')
    const moreCount = validationResult.errors.length - 3
    const suffix = moreCount > 0 ? ` (+${moreCount} more)` : ''
    showSnackBar({ msg: `Invalid trap configuration XML: ${errorList}${suffix}`, error: true })
    return
  }

  try {
    await uploadTrapdConfiguration(file)
    await store.fetchTrapConfig()
    showSnackBar({ msg: 'Trap configuration uploaded successfully.' })
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Failed to upload trap configuration.'
    showSnackBar({ msg, error: true })
  }
}

onMounted(async () => {
  try {
    await store.fetchTrapConfig()
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Failed to retrieve trapd configuration.'
    showSnackBar({ msg, error: true })
  }
})
</script>

<style lang="scss" scoped>
.trap-configuration-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 60px 40px 25px 40px;

    .action {
      input {
        display: none;
      }
    }
  }

  .tab-container {
    padding: 0px 40px 0px 40px;
  }
}
</style>
