import axios from 'axios'
import type { CustomerResponse } from './types'

export async function getCustomers(signal: AbortSignal): Promise<CustomerResponse[]> {
  const response = await axios.get<CustomerResponse[]>('/api/v1/customers', { signal, timeout: 15000 })
  return response.data
}