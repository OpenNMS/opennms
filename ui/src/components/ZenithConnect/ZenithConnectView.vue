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
            <span class="title">Zenith Connect</span>
          </div>
          <div>
            <h3>Current Registrations</h3>
          </div>
          <div class="registrations-container">
            <table>
              <thead>
                <th>Registration Status</th>
                <th>Registered On</th>
                <th>Last Connected On</th>
                <th>Connected Status</th>
                <th>System ID</th>
                <th>Display Name</th>
                <th>Access Token</th>
                <th>Refresh Token</th>
                <th>Actions</th>
              </thead>
              <tbody>
                <tr v-for="reg of zenithConnectStore.registrations" :key="reg.id">
                  <td>
                    <div v-if="reg.success === true">
                      <div class="register-success">Success</div>
                    </div>
                    <div v-else>
                      <div class="register-failed">Failed</div>
                    </div>
                  </td>
                  <td>{{ reg.registrationDate ? fnsFormat(reg.registrationDate, 'yyyy-MM-dd HH:mm:ss') : '--' }}</td>
                  <td>{{ reg.lastConnected ? fnsFormat(reg.lastConnected, 'yyyy-MM-dd HH:mm:ss') : '--' }}</td>
                  <td>
                    <div v-if="reg.connected === true">
                      <div class="register-success">Connected</div>
                    </div>
                    <div v-else>
                      <div class="register-failed">Not Connected</div>
                    </div>
                  </td>
                  <td>{{ reg.nmsSystemId }}</td>
                  <td>{{ reg.nmsDisplayName }}</td>
                  <td>
                    <div>
                      {{ ellipsify(reg.accessToken ?? '', 30) }}
                      <FeatherButton
                        primary
                        icon="Copy Access Token"
                        @click.prevent="() => onCopyToken(reg.accessToken ?? '')"
                      >
                        <FeatherIcon :icon="icons.ContentCopy"/>
                      </FeatherButton>
                    </div>
                  </td>
                  <td>
                    <div>
                      {{ ellipsify(reg.refreshToken ?? '', 30) }}
                      <FeatherButton
                        primary
                        icon="Copy Refresh Token"
                        @click.prevent="() => onCopyToken(reg.refreshToken ?? '')"
                      >
                        <FeatherIcon :icon="icons.ContentCopy"/>
                      </FeatherButton>
                    </div>
                  </td>
                  <td>
                    <div>
                      <FeatherButton
                        primary
                        :disabled="!reg.nmsSystemId"
                        @click.prevent="() => onSendData(reg)"
                      >
                        Send Data
                      </FeatherButton>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="spacer"></div>
          <h3>Register</h3>
          <div>
            Register your Meridian instance with Zenith in order to send data:
            <div class="spacer"></div>
            <FeatherButton
              primary
              @click="gotoRegister"
            >
              Register with Zenith
            </FeatherButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { format as fnsFormat } from 'date-fns'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useSnackbar from '@/composables/useSnackbar'
import { ellipsify } from '@/lib/utils'
import { useMenuStore } from '@/stores/menuStore'
import { useZenithConnectStore } from '@/stores/zenithConnectStore'
import { BreadCrumb } from '@/types'
import { ZenithConnectRegistration } from '@/types/zenithConnect'
import ContentCopy from '@featherds/icon/action/ContentCopy'

const menuStore = useMenuStore()
const zenithConnectStore = useZenithConnectStore()
const router = useRouter()
const { showSnackBar } = useSnackbar()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect', to: '#', position: 'last' }
  ]
})

const icons = markRaw({
  ContentCopy
})

const onCopyToken = (token: string) => {
  navigator.clipboard
    .writeText(token)
    .then(() => {
      showSnackBar({
        msg: 'Token copied'
      })
    })
    .catch(() => {
      showSnackBar({
        msg: 'Failed to copy token.'
      })
    })
}

const onSendData = (reg?: ZenithConnectRegistration) => {
  if (reg && reg.nmsSystemId) {
    // fake for now
    reg.connected = true
    reg.lastConnected = new Date()

    showSnackBar({
      msg: `Sending data for system: ${reg.nmsSystemId}`
    })
  }
}

const gotoRegister = () => {
  router.push('zenith-connect/register')
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";
@import "@featherds/table/scss/table";

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
  }
}
</style>
