<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="zc-container">
        <div class="content-container">
          <div class="title-container">
            <span class="title">Zenith Connect</span>
          </div>
          <div>
            Register your OpenNMS instance with Zenith in order to send data.
          </div>
          <div class="spacer"></div>
          <FeatherExpansionPanel
            class="zc-register-steps-expansion-panel"
          >
            <template #title>
              <h4>Steps</h4>
            </template>
            <template #default>
              <div class="instructions">
                <ul>
                  <li>Confirm the Zenith Connect URL below. If desired, modify the display name for your OpenNMS instance.</li>
                  <li>Click Connect to Zenith</li>
                  <li>You will then be directed to Zenith to login with the Zenith user associated with your OpenNMS instance.</li>
                  <li>Zenith will register your user. You may need to enter your Zenith password again.</li>
                  <li>Zenith will display your Refresh Token which OpenNMS will need to connect with Zenith.</li>
                  <li>You can copy the token to the clipboard and manually enter it into OpenNMS, or...</li>
                  <li>
                    You will be given a link to return to OpenNMS which will automatically save this token in OpenNMS for you. Note, you
                    may need to open a separate tab, log into OpenNMS, then copy that link into that same browser tab for this to work.
                  </li>
                </ul>
              </div>
            </template>
          </FeatherExpansionPanel>

          <div class="spacer"></div>
          <div>
            <div>
              <FeatherInput
                label="Zenith Connect URL"
                @update:modelValue="(val: any) => zenithUrl = String(val)"
                :modelValue="zenithUrl"
                class="input"
              />
            </div>
            <div>
              <FeatherInput
                label="OpenNMS System ID"
                :disabled="true"
                :modelValue="systemId"
                class="input"
              />
            </div>
            <div>
              <FeatherInput
                label="OpenNMS System Display Name"
                @update:modelValue="(val: any) => displayName = String(val)"
                :modelValue="displayName"
                class="input"
              />
            </div>
            <div class="btns">
              <FeatherButton
                primary
                @click="onRegisterWithZenith"
              >
                  Register with Zenith
              </FeatherButton>

              <FeatherButton
                secondary
                @click="onViewRegistrations"
              >
                  View Registrations
              </FeatherButton>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherInput } from '@featherds/input'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useMonitoringSystemStore } from '@/stores/monitoringSystemStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const monitoringSystemStore = useMonitoringSystemStore()
const router = useRouter()
const { showSnackBar } = useSnackbar()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)
const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)
const zenithConnectBaseUrl = computed<string>(() => menuStore.mainMenu.zenithConnectBaseUrl)
const zenithConnectRelativeUrl = computed<string>(() => menuStore.mainMenu.zenithConnectRelativeUrl)
const zenithUrl = ref<string>(`${zenithConnectBaseUrl.value}${zenithConnectRelativeUrl.value}`)
const systemId = computed(() => monitoringSystemStore.mainMonitoringSystem?.id ?? '')
const systemLabel = computed(() => monitoringSystemStore.mainMonitoringSystem?.label ?? '')
const displayName = ref(systemLabel.value)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect', to: '/zenith-connect' },
    { label: 'Register', to: '#', position: 'last' }
  ]
})

const validateRegistration = () => {
  const fields = []

  if (!zenithUrl.value) {
    fields.push('Zenith Connect URL')
  }

  if (!systemId.value) {
    fields.push('OpenNMS System ID')
  }

  if (!displayName.value) {
    fields.push('OpenNMS Display Name')
  }

  return fields
}

const onViewRegistrations = () => {
  router.push('/zenith-connect')
}

const onRegisterWithZenith = () => {
  const fields = validateRegistration()

  if (fields.length > 0) {
    const msg = fields.join(', ')

    showSnackBar({
      msg: `The following fields are required: ${msg}`,
      error: true
    })

    return
  }

  // Example callbackUrl: http://localhost:8980/opennms/ui/index.html#/zenith-connect/register-result
  const callbackUrl = `${baseHref.value}ui/index.html#/zenith-connect/register-result`

  const queryString = `?systemId=${encodeURIComponent(systemId.value)}&displayName=${encodeURIComponent(displayName.value)}&callbackUrl=${encodeURIComponent(callbackUrl)}`

  const url = `${zenithUrl.value}${queryString}`

  window.location.assign(url)
}

const fetchZenithUrls = async () => {
  if (!homeUrl.value || !baseHref.value) {
    await menuStore.getMainMenu()
    zenithUrl.value = `${zenithConnectBaseUrl.value}${zenithConnectRelativeUrl.value}`
  }
}

const fetchDisplayName = async () => {
  if (!monitoringSystemStore.mainMonitoringSystem) {
    await monitoringSystemStore.getMainMonitoringSystem()
    displayName.value = systemLabel.value
  }
}

onMounted(async () => {
  await Promise.allSettled([fetchZenithUrls(), fetchDisplayName()])
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.card {
  background: var($surface);
  padding: 0px 20px 20px 20px;

  .zc-container {
    display: flex;

    .zc-register-steps-expansion-panel {
      width: 50%;
    }

    .content-container {
      width: 35rem;
      flex: auto;

      .title-container {
        display: flex;
        justify-content: space-between;

        .title {
          @include headline1;
          margin: 16px 0px 16px 19px;
          display: block;
        }
      }

      .instructions {
        width: 95%;
      }

      .input {
        width: 50%;
      }

      .spacer {
        margin-bottom: 1rem;
      }

      .btns {
        display: flex;
        flex-direction: row;
      }
    }
  }
}
</style>
