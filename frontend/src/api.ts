import axios from 'axios'
import type { CreateCustomerRequest, CustomerResponse } from './types'

export async function getCustomers(signal: AbortSignal): Promise<CustomerResponse[]> {
  const response = await axios.get<CustomerResponse[]>('/api/v1/customers', { signal, timeout: 15000 })
  return response.data
}

export async function createCustomer(customer: CreateCustomerRequest): Promise<CustomerResponse> {
  const response = await axios.post<CustomerResponse>('/api/v1/customers', customer, { timeout: 15000 })
  return response.data
}