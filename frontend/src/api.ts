import axios from 'axios'
import type { CreateCustomerRequest, CustomerFieldErrors, CustomerResponse, FieldValidationError, ProblemDetail } from './types'
import { customerFields } from './validation'

export async function getCustomers(signal: AbortSignal): Promise<CustomerResponse[]> {
  const response = await axios.get<CustomerResponse[]>('/api/v1/customers', { signal, timeout: 15000 })
  return response.data
}

export async function createCustomer(customer: CreateCustomerRequest): Promise<CustomerResponse> {
  const response = await axios.post<CustomerResponse>('/api/v1/customers', customer, { timeout: 15000 })
  return response.data
}

function isFieldValidationError(value: unknown): value is FieldValidationError {
  return typeof value === 'object' && value !== null
    && 'field' in value && typeof value.field === 'string'
    && 'message' in value && typeof value.message === 'string' && value.message.trim().length > 0
}

function isProblemDetail(value: unknown): value is ProblemDetail {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) return false
  const problem = value as Record<string, unknown>
  return ['type', 'title', 'detail', 'instance'].every(key => problem[key] === undefined || typeof problem[key] === 'string')
    && (problem.status === undefined || typeof problem.status === 'number')
    && (problem.errors === undefined || (Array.isArray(problem.errors) && problem.errors.every(isFieldValidationError)))
}

export function getCreationFailure(failure: unknown): { fieldErrors: CustomerFieldErrors, message: string } {
  const fieldErrors: CustomerFieldErrors = {}
  const uncertain = 'Creation could not be confirmed. Check the customer list before trying again.'
  if (!axios.isAxiosError<unknown>(failure)) return { fieldErrors, message: `An unexpected error occurred. ${uncertain}` }
  if (failure.code === 'ECONNABORTED' || failure.code === 'ETIMEDOUT') {
    return { fieldErrors, message: `The request timed out. ${uncertain}` }
  }
  if (!failure.response) return { fieldErrors, message: `Could not reach the server. ${uncertain}` }
  if (failure.response.status >= 500) return { fieldErrors, message: `The server encountered a problem. ${uncertain}` }

  const problem = failure.response.data
  if (isProblemDetail(problem)) {
    for (const error of problem.errors ?? []) {
      const field = customerFields.find(candidate => candidate === error.field)
      if (field && !fieldErrors[field]) fieldErrors[field] = error.message
    }
    if (Object.keys(fieldErrors).length > 0) return { fieldErrors, message: 'Please correct the highlighted fields.' }
    if (problem.detail?.trim()) return { fieldErrors, message: problem.detail }
  }
  return { fieldErrors, message: 'Customer could not be created. Please check your entries and try again.' }
}