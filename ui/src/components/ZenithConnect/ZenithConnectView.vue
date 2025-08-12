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
                <th>Active</th>
                <th>System ID</th>
                <th>Display Name</th>
                <th>Access Token</th>
                <th>Refresh Token</th>
                <th>Actions</th>
              </thead>
              <tbody>
                <tr>
                  <td>
                    <div v-if="currentRegistration?.registered">
                      <div class="register-success">Registered</div>
                    </div>
                    <div v-else>
                      <div class="register-failed">Unregistered</div>
                    </div>
                  </td>
                  <td>{{ formatRegistrationDate(currentRegistration) }}</td>
                  <td>
                    <div v-if="currentRegistration?.active">
                      <div class="register-success">Active</div>
                    </div>
                    <div v-else>
                      <div class="register-failed">Inactive</div>
                    </div>
                  </td>
                  <td>{{ currentRegistration?.systemId }}</td>
                  <td>{{ currentRegistration?.displayName }}</td>
                  <td>
                    <div>
                      {{ ellipsify(currentRegistration?.accessToken ?? '', 30) }}
                      <FeatherButton
                        primary
                        icon="Copy Access Token"
                        @click.prevent="() => onCopyToken(currentRegistration?.accessToken ?? '')"
                      >
                        <FeatherIcon :icon="icons.ContentCopy"/>
                      </FeatherButton>
                    </div>
                  </td>
                  <td>
                    <div>
                      {{ ellipsify(currentRegistration?.refreshToken ?? '', 30) }}
                      <FeatherButton
                        primary
                        icon="Copy Refresh Token"
                        @click.prevent="() => onCopyToken(currentRegistration?.refreshToken ?? '')"
                      >
                        <FeatherIcon :icon="icons.ContentCopy"/>
                      </FeatherButton>
                    </div>
                  </td>
                  <td>
                    <div>
                      <FeatherButton
                        primary
                        :disabled="!currentRegistration?.registered || !currentRegistration?.systemId"
                        @click.prevent="() => onSendData(currentRegistration)"
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
const currentRegistration = computed<ZenithConnectRegistration | undefined>(() => zenithConnectStore.currentRegistration)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect', to: '#', position: 'last' }
  ]
})

const icons = markRaw({
  ContentCopy
})

const formatRegistrationDate = (reg?: ZenithConnectRegistration) => {
  if (reg?.createTimeMs) {
    const date = new Date(reg.createTimeMs)

    return fnsFormat(date, 'yyyy-MM-dd HH:mm:ss')
  }

  return '--'
}

const onCopyToken = async (token: string) => {
  try {
    await navigator.clipboard.writeText(token)

    showSnackBar({
      msg: 'Token copied'
    })
  } catch {
    showSnackBar({
      msg: 'Failed to copy token.'
    })
  }
}

const onSendData = (reg?: ZenithConnectRegistration) => {
  if (reg && reg.registered && reg.systemId) {
    // TODO: fake for now, should set 'active' in DB and possibly notify exporter process
    reg.active = true

    showSnackBar({
      msg: `Sending data for ${reg.displayName} (${reg.systemId})`
    })
  }
}

const gotoRegister = () => {
  router.push('/zenith-connect/register')
}

onMounted(async () => {
  await zenithConnectStore.fetchRegistrations()
})
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
