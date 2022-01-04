import { IPRange, ProvisionRequest, SNMPDetectRequest, VuexContext } from '@/types'
import { State } from './state'
import API from '../../services'

const setShowCompleteButton = (context: VuexContext, val: boolean | string) => {
  context.commit('SET_SHOW_COMPLETE_BUTTON', val)
}

const showAddStepNextButton = (context: VuexContext, bool: boolean) => {
  context.commit('SET_SHOW_ADD_STEP_NEXT_BUTTON', bool)
}

const showConfigureServiceStepNextButton = (context: VuexContext, bool: boolean) => {
  context.commit('SET_SHOW_CONFIGURE_SERVICE_STEP_NEXT_BUTTON', bool)
}

const addCompletedService = (context: VuexContext, service: string) => {
  context.commit('ADD_COMPLETED_SERVICE', service)
}

const scanIPRanges = async (context: VuexContext, ipRanges: IPRange[]) => {
  const successfulResp = await API.scanIPRanges(ipRanges)

  context.commit('SAVE_IP_RANGE_SCAN_REQ', ipRanges)

  if (successfulResp) {
    context.commit('SAVE_IP_RANGE_SCAN_RESPONSE', successfulResp)
  }

  return successfulResp
}

const detectSNMPAvailable = async (context: VuexContext, SNMPDetectRequestObjects: SNMPDetectRequest[]) => {
  const successfulResp = await API.detectSNMPAvailable(SNMPDetectRequestObjects)

  context.commit('SAVE_SNMP_DETECT_REQ', SNMPDetectRequestObjects)

  if (successfulResp) {
    context.commit('SAVE_SNMP_DETECT_RESPONSE', successfulResp)
  }

  return successfulResp
}

const provision = async (context: VuexContext, provisionRequest: ProvisionRequest) => {
  return await API.provision(provisionRequest)
}

const saveProvisionRequest = (context: VuexContext, provisionRequest: ProvisionRequest) => {
  context.commit('SAVE_PROVISION_REQUEST', provisionRequest)
}

const scheduleProvision = async ({ state }: { state: State }) => {
  return await API.provision(state.provisionRequest)
}

export default {
  scanIPRanges,
  detectSNMPAvailable,
  addCompletedService,
  setShowCompleteButton,
  showAddStepNextButton,
  showConfigureServiceStepNextButton,
  provision,
  saveProvisionRequest,
  scheduleProvision
}
