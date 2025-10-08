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
          <div class="title-search">
            <span class="title">Zenith Connect Registration Result</span>
          </div>
          <div class="spacer"></div>
          <div v-if="isProcessing">
            <div class="processing-status">{{ processingStatus }}</div>
          </div>
          <div v-else>
            <div>
              <h3>Registration Results</h3>
            </div>
            <div class="results-container">
              <table>
                <thead>
                  <th>Result</th>
                  <th>System ID</th>
                  <th>Display Name</th>
                  <th>Access Token</th>
                  <th>Refresh Token</th>
                </thead>
                <tbody>
                  <tr>
                    <td>
                      <div v-if="zenithConnectStore.registerResponse?.success === true">
                        <div class="register-success">Success</div>
                      </div>
                      <div v-else>
                        <div class="register-failed">Failed</div>
                      </div>
                    </td>
                    <td>{{ zenithConnectStore.currentRegistration?.systemId }}</td>
                    <td>{{ zenithConnectStore.currentRegistration?.displayName }}</td>
                    <td>
                      <div>
                        {{ ellipsify(zenithConnectStore.currentRegistration?.accessToken ?? '', 30) }}
                        <FeatherButton
                          primary
                          icon="Copy Access Token"
                          @click.prevent="() => onCopyToken(true)"
                        >
                          <FeatherIcon :icon="icons.ContentCopy"/>
                        </FeatherButton>
                      </div>
                    </td>
                    <td>
                      <div>
                        {{ ellipsify(zenithConnectStore.currentRegistration?.refreshToken ?? '', 30) }}
                        <FeatherButton
                          primary
                          icon="Copy Refresh Token"
                          @click.prevent="() => onCopyToken(false)"
                        >
                          <FeatherIcon :icon="icons.ContentCopy"/>
                        </FeatherButton>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div>
            <div class="spacer"></div>
            <FeatherButton
              primary
              @click="gotoView"
            >
                View Zenith Connections
            </FeatherButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import ContentCopy from '@featherds/icon/action/ContentCopy'
import { useRoute } from 'vue-router'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { ellipsify } from '@/lib/utils'
import { useMenuStore } from '@/stores/menuStore'
import { useZenithConnectStore } from '@/stores/zenithConnectStore'
import { BreadCrumb } from '@/types'
import { ZenithConnectRegistration, ZenithConnectRegistrationResponse } from '@/types/zenithConnect'

const menuStore = useMenuStore()
const zenithConnectStore = useZenithConnectStore()
const route = useRoute()
const router = useRouter()
const { startSpinner, stopSpinner } = useSpinner()

const isProcessing = ref(false)
const processingStatus = ref('')
const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)
const { showSnackBar } = useSnackbar()

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect', to: '/zenith-connect' },
    { label: 'Zenith Connect Registration Result', to: '#', position: 'last' }
  ]
})

const icons = markRaw({
  ContentCopy
})

const gotoView = () => {
  router.push('/zenith-connect')
}

const onCopyToken = async (isAccessToken: boolean) => {
  const token = (isAccessToken ? zenithConnectStore.registerResponse?.accessToken : zenithConnectStore.registerResponse?.refreshToken) ?? ''

  try {
    await navigator.clipboard.writeText(token)

    showSnackBar({
      msg: `${isAccessToken ? 'Access' : 'Refresh'} token copied.`
    })
  } catch {
    displayError('Failed to copy token.')
  }
}

// parse the registration response from Zenith
const parseRegistrationResponse = () => {
  const response = {
    success: route.query.success && route.query.success === 'true' ? true : false,
    nmsSystemId: route.query.nmsSystemId ?? '',
    nmsDisplayName: route.query.nmsDisplayName ?? '',
    accessToken: route.query.accessToken ?? '',
    refreshToken: route.query.refreshToken ?? ''
  } as ZenithConnectRegistrationResponse

  return response
}

const displayError = (msg?: string) => {
  showSnackBar({
    msg: msg ?? 'Error registering with Zenith.',
    error: true
  })
}

const processRegistrationResponse = async () => {
  let status = false
  isProcessing.value = true
  processingStatus.value = 'Processing Zenith Registration response...'
  startSpinner()
  
  try {
    const response: ZenithConnectRegistrationResponse = parseRegistrationResponse()
    zenithConnectStore.setRegistrationResponse(response)

    if (!response.success || !response.refreshToken) {
      displayError()
      return false
    }

    const registration = {
      systemId: response.nmsSystemId,
      displayName: response.nmsDisplayName,
      zenithHost: menuStore.mainMenu.zenithConnectBaseUrl,
      zenithRelativeUrl: menuStore.mainMenu.zenithConnectRelativeUrl,
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      registered: true,
      active: false
    } as ZenithConnectRegistration

    processingStatus.value = 'Saving registration...'

    const addResponse = await zenithConnectStore.addRegistration(registration)

    if (!addResponse) {
      displayError()
      return false
    }

    processingStatus.value = 'Getting registrations...'
  
    const fetchResponse = await zenithConnectStore.fetchRegistrations()

    if (!fetchResponse) {
      displayError()
      return false
    }

    status = true
  } catch (e) {
    showSnackBar({
      msg: 'Error registering with Zenith.',
      error: true
    })
  } finally {
    stopSpinner()
    isProcessing.value = false
    processingStatus.value = ''
  }

  return status
}

onMounted(async () => {
  const status = await processRegistrationResponse()

  // if registration was successful, redirect to view page
  if (status) {
    showSnackBar({
      msg: 'Registration was successful. Redirecting to View page...',
      timeout: 4000
    })

    window.setTimeout(() => {
      router.push('/zenith-connect')
    }, 5000)
  }
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";
@import "@featherds/table/scss/table";

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
  }

  .processing-status {
    font-weight: bold;
  }

  .register-success {
    background-color: var($success);
    color: white;
    border-radius: 5px;
    text-align: center;
    font-weight: bold;
  }

  .register-failure {
    background-color: var($error);
    color: white;
    border-radius: 5px;
    text-align: center;
    font-weight: bold;
  }

  table {
    @include table();
    &.condensed {
      @include table-condensed();
    }
    margin-top: 0px;
  }

  .spacer {
    margin-bottom: 1rem;
  }
}
</style>
