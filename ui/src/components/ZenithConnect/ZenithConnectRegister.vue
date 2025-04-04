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
          <div class="spacer"></div>
          <div>
            Register your Meridian instance with Zenith in order to send data.
          </div>
          <h3>Steps</h3>
          <div class="instructions">
            <ul>
              <li>Enter the Zenith base URL below.</li>
              <li>If desired, enter a friendly display name for your Meridian instance.</li>
              <li>Click Connect to Zenith</li>
              <li>You will then be directed to Zenith to login with the Zenith user associated with your Meridian instance.</li>
              <li>Zenith will register your user. You may need to enter your Zenith password again.</li>
              <li>Zenith will display your Refresh Token which Meridian will need to connect with Zenith.</li>
              <li>You can copy the token to the clipboard and manually enter it into Meridian, or...</li>
              <li>
                You will be given a link to return to Meridian which will automatically save this token in Meridian for you. Note, you
                may need to open a separate tab, log into Meridian, then copy that link into that same browser tab for this to work.
              </li>
            </ul>
          </div>
          <div class="spacer"></div>
          <div>
            <div>
              <FeatherInput
                label="Zenith Base URL"
                @update:modelValue="(val: any) => zenithUrl = String(val)"
                :modelValue="zenithUrl"
                class="input"
              />
            </div>
            <div>
              <FeatherInput
                label="Meridian System ID"
                @update:modelValue="(val: any) => systemId = String(val)"
                :modelValue="systemId"
                class="input"
              />
            </div>
            <div>
              <FeatherInput
                label="Meridian Display Name"
                @update:modelValue="(val: any) => displayName = String(val)"
                :modelValue="displayName"
                class="input"
              />
            </div>
            <div class="spacer"></div>
            <div>
              <div>
                You will be redirected to Zenith's <strong>Zenith Connect</strong> where you will need to enter your Zenith password
                for the Meridian user associated with Zenith.
              </div>
              <div class="spacer"></div>
              <FeatherButton
                primary
                @click="onRegisterWithZenith"
              >
                  Register with Zenith
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
import { FeatherInput } from '@featherds/input'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useMonitoringSystemStore } from '@/stores/monitoringSystemStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const monitoringSystemStore = useMonitoringSystemStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)
const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)
const zenithConnectBaseUrl = computed<string>(() => menuStore.mainMenu.zenithConnectBaseUrl)
const zenithConnectRelativeUrl = computed<string>(() => menuStore.mainMenu.zenithConnectRelativeUrl)
const zenithUrl = computed<string>(() => `${zenithConnectBaseUrl.value}${zenithConnectRelativeUrl.value}`)
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

const onRegisterWithZenith = () => {
  // Example callbackUrl: http://localhost:8980/opennms/ui/index.html#/zenith-connect/register
  const callbackUrl = `${baseHref.value}ui/index.html#/zenith-connect/register-result`

  const queryString = `?systemId=${encodeURIComponent(systemId.value)}&displayName=${encodeURIComponent(displayName.value)}&callbackUrl=${encodeURIComponent(callbackUrl)}`

  const url = `${zenithUrl.value}${queryString}`

  window.location.assign(url)
}

onMounted(async () => {
  if (!homeUrl.value || !baseHref.value) {
    menuStore.getMainMenu()
  }

  if (!monitoringSystemStore.mainMonitoringSystem) {
    monitoringSystemStore.getMainMonitoringSystem()
  }
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

.card {
  background: var($surface);
  padding: 0px 20px 20px 20px;

  .zc-container {
    display: flex;

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
        width: 70%;
      }

      .input {
        width: 50%;
      }

      .spacer {
        margin-bottom: 1rem;
      }
    }
  }
}
</style>
