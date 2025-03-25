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
            <span class="title">Zenith Register Result</span>
          </div>
          <div class="spacer"></div>
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
                  <td>{{ zenithConnectStore.registerResponse?.nmsSystemId }}</td>
                  <td>{{ zenithConnectStore.registerResponse?.nmsDisplayName }}</td>
                  <td>
                    <div>
                      {{ ellipsify(zenithConnectStore.registerResponse?.accessToken ?? '', 30) }}
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
                      {{ ellipsify(zenithConnectStore.registerResponse?.refreshToken ?? '', 30) }}
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
import { v4 as uuidv4 } from 'uuid'
import { useRoute } from 'vue-router'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useSnackbar from '@/composables/useSnackbar'
import { ellipsify } from '@/lib/utils'
import { useMenuStore } from '@/stores/menuStore'
import { useZenithConnectStore } from '@/stores/zenithConnectStore'
import { BreadCrumb } from '@/types'
import { ZenithConnectRegisterResponse, ZenithConnectRegistration } from '@/types/zenithConnect'

const menuStore = useMenuStore()
const zenithConnectStore = useZenithConnectStore()
const route = useRoute()
const router = useRouter()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)
const { showSnackBar } = useSnackbar()

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect', to: '/zenith-connect' },
    { label: 'Zenith Connect Register Result', to: '#', position: 'last' }
  ]
})

const icons = markRaw({
  ContentCopy
})

const gotoView = () => {
  router.push('/zenith-connect')
}

const onCopyToken = (isAccessToken: boolean) => {
  const token = (isAccessToken ? zenithConnectStore.registerResponse?.accessToken : zenithConnectStore.registerResponse?.refreshToken) ?? ''

  navigator.clipboard
    .writeText(token)
    .then(() => {
      showSnackBar({
        msg: `${isAccessToken ? 'Access' : 'Refresh'} token copied.`
      })
    })
    .catch(() => {
      showSnackBar({
        msg: 'Failed to copy token.'
      })
    })
}

onMounted(() => {
  // TODO: This is all mocked up, needs real implementation once we actually save registrations
  const response = {
    success: route.query.success && route.query.success === 'true' ? true : false,
    nmsSystemId: route.query.nmsSystemId ?? '',
    nmsDisplayName: route.query.nmsDisplayName ?? '',
    accessToken: route.query.accessToken ?? '',
    refreshToken: route.query.refreshToken ?? ''
  } as ZenithConnectRegisterResponse

  zenithConnectStore.setRegisterResponse(response)

  // add to registrations
  const registration = {
    ...response,
    id: uuidv4(),
    registrationDate: new Date(),
    lastConnected: new Date(),
    connected: false
  } as ZenithConnectRegistration

  zenithConnectStore.addRegistration(registration)
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
