import { AxiosError, AxiosHeaders } from 'axios'
import { describe, expect, it } from 'vitest'
import { getCreationFailure } from './api'
import { validateCustomer } from './validation'
import type { CreateCustomerRequest } from './types'

const validCustomer: CreateCustomerRequest = { firstName: 'Ada', lastName: 'Lovelace', dateOfBirth: '1990-12-10' }
const now = new Date('2026-10-11T00:30:00Z')

function responseFailure(data: unknown, status = 400) {
  const failure = new AxiosError('Request failed')
  failure.response = { data, status, statusText: 'Error', headers: {}, config: { headers: new AxiosHeaders() } }
  return failure
}

describe('client validation', () => {
  it.each(['firstName', 'lastName'] as const)('validates %s after trimming and permits exactly 50 characters', field => {
    expect(validateCustomer({ ...validCustomer, [field]: '  \t ' }, now)[field]).toContain('is required')
    expect(validateCustomer({ ...validCustomer, [field]: 'a'.repeat(51) }, now)[field]).toContain('50 characters')
    expect(validateCustomer({ ...validCustomer, [field]: `  ${'a'.repeat(50)}  ` }, now)).toEqual({})
  })

  it.each(['', 'not-a-date', '2026-2-03', '2025-02-29', '2026-02-30', '2026-13-01', '2026-10-10T00:00:00Z'])('rejects missing or invalid dates: %s', dateOfBirth => {
    expect(validateCustomer({ ...validCustomer, dateOfBirth }, now).dateOfBirth).toBeDefined()
  })

  it.each(['2026-10-11', '2026-10-12'])('rejects today and future dates: %s', dateOfBirth => {
    expect(validateCustomer({ ...validCustomer, dateOfBirth }, now).dateOfBirth).toBe('Birth date must be before today.')
  })

  it('uses UTC at local-date boundaries, accepts leap days and imposes no age limit', () => {
    const boundary = new Date('2026-10-10T20:30:00-04:00')
    expect(validateCustomer({ ...validCustomer, dateOfBirth: '2026-10-10' }, boundary)).toEqual({})
    expect(validateCustomer({ ...validCustomer, dateOfBirth: '2026-10-11' }, boundary).dateOfBirth).toBeDefined()
    expect(validateCustomer({ ...validCustomer, dateOfBirth: '2024-02-29' }, now)).toEqual({})
    expect(validateCustomer({ ...validCustomer, dateOfBirth: '1800-01-01' }, now)).toEqual({})
  })
})

describe('creation failures', () => {
  it('maps only recognised fields and keeps the first message per field', () => {
    expect(getCreationFailure(responseFailure({ errors: [
      { field: 'firstName', message: 'First name is invalid.' },
      { field: 'firstName', message: 'Another error.' },
      { field: 'dateOfBirth', message: 'Birth date must be in the past.' },
      { field: '__proto__', message: 'Unrecognised field.' },
    ] }))).toEqual({ fieldErrors: { firstName: 'First name is invalid.', dateOfBirth: 'Birth date must be in the past.' }, message: 'Please correct the highlighted fields.' })
  })

  it('uses safe backend general details when no recognised fields are provided', () => {
    expect(getCreationFailure(responseFailure({ detail: 'One or more customer values are invalid' })).message).toBe('One or more customer values are invalid')
  })

  it.each([null, '<html>Bad Gateway</html>', [], { errors: 'invalid' }, { errors: [null] }, { errors: [{ field: 'firstName', message: 42 }] }, { detail: { unexpected: 'object' } }])('falls back safely for malformed responses: %j', data => {
    const result = getCreationFailure(responseFailure(data))
    expect(result.fieldErrors).toEqual({})
    expect(result.message).toContain('Please check your entries')
  })

  it('distinguishes network errors, timeouts, server errors, and unexpected failures', () => {
    expect(getCreationFailure(new AxiosError('Network failure', 'ERR_NETWORK')).message).toContain('Could not reach the server')
    expect(getCreationFailure(new AxiosError('Timeout', 'ECONNABORTED')).message).toContain('timed out')
    expect(getCreationFailure(new AxiosError('Timeout', 'ETIMEDOUT')).message).toContain('timed out')
    const server = getCreationFailure(responseFailure({ detail: 'Private stack trace' }, 500))
    expect(server.message).toContain('server encountered a problem')
    expect(server.message).not.toContain('Private stack trace')
    expect(getCreationFailure(new Error('Unexpected')).message).toContain('unexpected error')
  })
})