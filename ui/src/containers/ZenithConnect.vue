<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <div class="zc-container">
          <div class="content-container">
            <div class="title-search">
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
                  @update:modelValue="(val) => zenithUrl = String(val)"
                  :modelValue="zenithUrl"
                  class="input"
                />
              </div>
              <div>
                <FeatherInput
                  label="Meridian Display Name"
                  @update:modelValue="(val) => displayName = String(val)"
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
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

// TODO: Put this in configuration and Menu Rest API
const ZENITH_CONNECT_BASE_URL = 'https://onmshs.local:1443'
const ZENITH_CONNECT_REGISTER_RELATIVE_URL = '/zenith-connect'

const menuStore = useMenuStore()

const zenithUrl = ref(`${ZENITH_CONNECT_BASE_URL}${ZENITH_CONNECT_REGISTER_RELATIVE_URL}`)
const systemId = ref('')
const displayName = ref('')

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)
const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect', to: '#', position: 'last' }
  ]
})

const onRegisterWithZenith = () => {
  console.log('onRegisterWithZenith')

  // TODO: Get this from DB
  systemId.value = '12345'
  // Example callbackUrl: http://localhost:8980/opennms/zenith-connect-success/index.jsp`
  const callbackUrl = `${baseHref.value}zenith-connect-success/index.jsp`

  console.log(`| setting callbackUrl: ${callbackUrl}`)
  console.log(`| window.location.origin was; ${window.location.origin}`)

  const queryString = `?systemId=${encodeURIComponent(systemId.value)}&displayName=${encodeURIComponent(displayName.value)}&callbackUrl=${encodeURIComponent(callbackUrl)}`

  const url = `${zenithUrl.value}${queryString}`

  console.log(`| redirecting to: ${url}`)

  window.location.assign(url)
}

onMounted(async () => {
  // some debugging info
  console.log('ZenithConnect loaded')
  console.log('window.location:')
  console.dir(window.location)

  console.log('| homeUrl:')
  console.dir(homeUrl.value)
  console.log('| baseHref:')
  console.dir(baseHref.value)

  if (!homeUrl.value || !baseHref.value) {
    menuStore.getMainMenu().then(() => {
      console.log('got main menu, hopefully')
    
      console.log('| homeUrl:')
      console.dir(homeUrl.value)
      console.log('| baseHref:')
      console.dir(baseHref.value)
    })
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

      .title-search {
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
