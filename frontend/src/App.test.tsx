import { StrictMode } from 'react'
import { act, cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import { createCustomer, getCustomers } from './api'
import type { CustomerResponse } from './types'

vi.mock('./api', () => ({ getCustomers: vi.fn(), createCustomer: vi.fn() }))

const existingCustomer: CustomerResponse = { id: 1, firstName: 'Viper', lastName: 'Napuatzi', dateOfBirth: '1985-04-12' }
const newCustomer: CustomerResponse = { id: 2, firstName: 'Ada', lastName: 'Lovelace', dateOfBirth: '1990-12-10' }

function deferred<Value>() {
  let resolve!: (value: Value) => void
  let reject!: (reason: Error) => void
  const promise = new Promise<Value>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })
  return { promise, resolve, reject }
}

async function fillForm() {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('First name'), 'Ada')
  await user.type(screen.getByLabelText('Last name'), 'Lovelace')
  fireEvent.change(screen.getByLabelText('Birth date'), { target: { value: '1990-12-10' } })
  return user
}

function expectFormValues(firstName = 'Ada', lastName = 'Lovelace', dateOfBirth = '1990-12-10') {
  expect((screen.getByLabelText('First name') as HTMLInputElement).value).toBe(firstName)
  expect((screen.getByLabelText('Last name') as HTMLInputElement).value).toBe(lastName)
  expect((screen.getByLabelText('Birth date') as HTMLInputElement).value).toBe(dateOfBirth)
}

beforeEach(() => {
  vi.resetAllMocks()
  vi.mocked(getCustomers).mockResolvedValue([existingCustomer])
  vi.mocked(createCustomer).mockResolvedValue(newCustomer)
})

afterEach(cleanup)

describe('customer creation', () => {
  it('submits a date-only payload, clears after success, and shows the refreshed customer first', async () => {
    render(<App />)
    await screen.findByText('Viper')
    vi.mocked(getCustomers).mockResolvedValue([newCustomer, existingCustomer])
    const user = await fillForm()
    await user.click(screen.getByRole('button', { name: 'Create customer' }))
    await screen.findByText('Ada')
    expect(createCustomer).toHaveBeenCalledWith({ firstName: 'Ada', lastName: 'Lovelace', dateOfBirth: '1990-12-10' })
    expectFormValues('', '', '')
    const rows = screen.getAllByRole('row')
    expect(within(rows[1]).getByText('Ada')).toBeDefined()
    expect(within(rows[2]).getByText('Viper')).toBeDefined()
  })

  it('keeps values and rows while pending and guards repeated submissions', async () => {
    const creation = deferred<CustomerResponse>()
    vi.mocked(createCustomer).mockReturnValue(creation.promise)
    render(<App />)
    await screen.findByText('Viper')
    const user = await fillForm()
    const form = screen.getByLabelText('First name').closest('form')!
    fireEvent.submit(form)
    fireEvent.submit(form)
    await user.click(screen.getByRole('button', { name: 'Creating...' }))
    expect(createCustomer).toHaveBeenCalledTimes(1)
    expect((screen.getByRole('button', { name: 'Creating...' }) as HTMLButtonElement).disabled).toBe(true)
    expectFormValues()
    expect(screen.getByText('Viper')).toBeDefined()
    await act(async () => creation.resolve(newCustomer))
    await waitFor(() => expect((screen.getByRole('button', { name: 'Create customer' }) as HTMLButtonElement).disabled).toBe(false))
  })

  it('preserves entries and records after POST failure and allows a successful retry', async () => {
    vi.mocked(createCustomer).mockRejectedValueOnce(new Error('Network unavailable'))
    render(<App />)
    await screen.findByText('Viper')
    const user = await fillForm()
    await user.click(screen.getByRole('button', { name: 'Create customer' }))
    await screen.findByRole('alert')
    expectFormValues()
    expect(screen.getByText('Viper')).toBeDefined()
    expect(getCustomers).toHaveBeenCalledTimes(1)
    vi.mocked(getCustomers).mockResolvedValue([newCustomer, existingCustomer])
    await user.click(screen.getByRole('button', { name: 'Create customer' }))
    await screen.findByText('Ada')
    expect(createCustomer).toHaveBeenCalledTimes(2)
    expect(screen.queryByRole('alert')).toBeNull()
    expectFormValues('', '', '')
  })

  it('separates confirmed creation from failed refresh and retries GET without another POST', async () => {
    render(<App />)
    await screen.findByText('Viper')
    vi.mocked(getCustomers).mockRejectedValueOnce(new Error('Refresh unavailable'))
    const user = await fillForm()
    await user.click(screen.getByRole('button', { name: 'Create customer' }))
    expect((await screen.findByRole('alert')).textContent).toContain('Customer created, but the list could not be refreshed')
    expectFormValues('', '', '')
    expect(screen.getByText('Viper')).toBeDefined()
    vi.mocked(getCustomers).mockResolvedValue([newCustomer, existingCustomer])
    await user.click(screen.getByRole('button', { name: 'Retry' }))
    await screen.findByText('Ada')
    expect(createCustomer).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('cancels an older refresh so its result cannot overwrite the post-creation list', async () => {
    const olderRead = deferred<CustomerResponse[]>()
    render(<App />)
    await screen.findByText('Viper')
    vi.mocked(getCustomers).mockReturnValueOnce(olderRead.promise)
    const user = await fillForm()
    await user.click(screen.getByRole('button', { name: 'Refresh' }))
    const olderSignal = vi.mocked(getCustomers).mock.calls[1][0]
    vi.mocked(getCustomers).mockResolvedValue([newCustomer, existingCustomer])
    await user.click(screen.getByRole('button', { name: 'Create customer' }))
    await screen.findByText('Ada')
    expect(olderSignal.aborted).toBe(true)
    await act(async () => olderRead.resolve([existingCustomer]))
    expect(screen.getByText('Ada')).toBeDefined()
  })

  it('refreshes after creation even when the initial list request is still pending', async () => {
    const initialRead = deferred<CustomerResponse[]>()
    vi.mocked(getCustomers).mockReturnValueOnce(initialRead.promise)
    render(<App />)
    const initialSignal = vi.mocked(getCustomers).mock.calls[0][0]
    const user = await fillForm()
    vi.mocked(getCustomers).mockResolvedValue([newCustomer])
    await user.click(screen.getByRole('button', { name: 'Create customer' }))
    await screen.findByText('Ada')
    expect(initialSignal.aborted).toBe(true)
    await act(async () => initialRead.resolve([]))
    expect(screen.getByText('Ada')).toBeDefined()
  })

  it('ignores StrictMode cancellation instead of displaying a loading error', async () => {
    vi.mocked(getCustomers).mockImplementation(async signal => {
      if (signal.aborted) throw new Error('Aborted')
      return [existingCustomer]
    })
    render(<StrictMode><App /></StrictMode>)
    await screen.findByText('Viper')
    expect(screen.queryByRole('alert')).toBeNull()
  })
})