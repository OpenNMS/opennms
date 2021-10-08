import { rest } from './axiosInstances'
import { IPRange, IPRangeResponse, ProvisionRequest, SNMPDetectRequest, SNMPDetectResponse } from "@/types"

const endpoint = '/nodediscover'

const scanIPRanges = async (IPRanges: IPRange[]): Promise<IPRangeResponse[] | false> => {
	try {
		const resp = await rest.post(`${endpoint}/scan`, IPRanges)
		return resp.data
	} catch (err) {
		return false
	}
}

const detectSNMPAvailable = async (SNMPDetectRequests: SNMPDetectRequest[]): Promise<SNMPDetectResponse[] | false> => {
	try {
		const resp = await rest.post(`${endpoint}/detect`, SNMPDetectRequests)
		return resp.data
	} catch (err) {
		return false
	}
}

const provision = async (provisionRequest: ProvisionRequest): Promise<string | false> => {
	try {
		const resp = await rest.post(`${endpoint}/provision`, provisionRequest)
		return resp.data
	} catch (err) {
		return false
	}
}

export {
	scanIPRanges,
	detectSNMPAvailable,
	provision
}