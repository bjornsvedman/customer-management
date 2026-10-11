import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { createCustomer, getCreationFailure, getCustomers } from './api'
import type { CreateCustomerRequest, CustomerFieldErrors, CustomerResponse } from './types'
import { customerFields, validateCustomer } from './validation'
import './App.css'

const emptyForm: CreateCustomerRequest = { firstName: '', lastName: '', dateOfBirth: '' }

function App() {
  const [customers, setCustomers] = useState<CustomerResponse[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const activeRequest = useRef<AbortController | null>(null)
  const [form, setForm] = useState<CreateCustomerRequest>(emptyForm)
  const [submitting, setSubmitting] = useState(false)
  const [creationError, setCreationError] = useState<string | null>(null)
  const [creationStatus, setCreationStatus] = useState('')
  const submissionPending = useRef(false)
  const [fieldErrors, setFieldErrors] = useState<CustomerFieldErrors>({})
  const formElement = useRef<HTMLFormElement>(null)
  const pendingFocus = useRef<keyof CreateCustomerRequest | null>(null)

  useEffect(() => {
    if (submitting || !pendingFocus.current) return
    const input = formElement.current?.elements.namedItem(pendingFocus.current)
    if (input instanceof HTMLInputElement) input.focus()
    pendingFocus.current = null
  }, [fieldErrors, submitting])

  function updateField(field: keyof CreateCustomerRequest, value: string) {
    setForm(previous => ({ ...previous, [field]: value }))
    setFieldErrors(previous => ({ ...previous, [field]: undefined }))
    setCreationError(null)
    setCreationStatus('')
  }

  function showFieldErrors(errors: CustomerFieldErrors) {
    pendingFocus.current = customerFields.find(field => errors[field]) ?? null
    setFieldErrors(errors)
  }

  useEffect(() => {
    const controller = new AbortController()
    activeRequest.current = controller
    getCustomers(controller.signal)
      .then(data => { if (!controller.signal.aborted) setCustomers(data) })
      .catch(() => {
        if (!controller.signal.aborted) setError('Customers could not be loaded. Please try again.')
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          activeRequest.current = null
          setLoading(false)
        }
      })
    return () => { controller.abort(); activeRequest.current?.abort() }
  }, [])

  async function refresh(afterCreation = false) {
    if (activeRequest.current && !afterCreation) return
    activeRequest.current?.abort()
    const controller = new AbortController()
    activeRequest.current = controller
    setLoading(true)
    setError(null)
    try {
      const data = await getCustomers(controller.signal)
      if (!controller.signal.aborted) setCustomers(data)
    } catch {
      if (!controller.signal.aborted) setError(afterCreation
        ? 'Customer created, but the list could not be refreshed. Retry to load the latest customers.'
        : 'Customers could not be refreshed. Please try again.')
    } finally {
      if (!controller.signal.aborted) {
        activeRequest.current = null
        setLoading(false)
      }
    }
  }

  async function submitCustomer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (submissionPending.current) return
    setCreationError(null)
    setCreationStatus('')
    const errors = validateCustomer(form)
    showFieldErrors(errors)
    if (Object.keys(errors).length > 0) {
      setCreationError('Please correct the highlighted fields.')
      return
    }
    submissionPending.current = true
    setSubmitting(true)
    try {
      await createCustomer({ ...form, firstName: form.firstName.trim(), lastName: form.lastName.trim() })
    } catch (failure: unknown) {
      const result = getCreationFailure(failure)
      showFieldErrors(result.fieldErrors)
      setCreationError(result.message)
      submissionPending.current = false
      setSubmitting(false)
      return
    }
    setForm(emptyForm)
    setCreationStatus('Customer created.')
    await refresh(true)
    submissionPending.current = false
    setSubmitting(false)
  }

  return (
    <>
      <a className="skip-link" href="#customers">Skip to customers</a>
      <header className="app-header"><div className="header-inner">
        <span className="app-name">Customer Management<span className="brand-dot" aria-hidden="true">.</span></span>
        <span className="header-label">Interview project</span>
      </div></header>
      <main id="customers">
        <section className="creation-section" aria-labelledby="create-heading">
          <h2 id="create-heading">Create customer</h2>
          <form ref={formElement} onSubmit={event => void submitCustomer(event)} aria-busy={submitting} noValidate>
            <fieldset disabled={submitting}>
              <legend className="sr-only">Customer details</legend>
              <div className="form-fields">
                <div className="form-field">
                  <label htmlFor="first-name">First name</label>
                  <input id="first-name" name="firstName" autoComplete="given-name" required aria-invalid={Boolean(fieldErrors.firstName)} aria-describedby={fieldErrors.firstName ? 'first-name-error' : undefined} value={form.firstName} onChange={event => updateField('firstName', event.target.value)} />
                  {fieldErrors.firstName && <p id="first-name-error" className="field-error">{fieldErrors.firstName}</p>}
                </div>
                <div className="form-field">
                  <label htmlFor="last-name">Last name</label>
                  <input id="last-name" name="lastName" autoComplete="family-name" required aria-invalid={Boolean(fieldErrors.lastName)} aria-describedby={fieldErrors.lastName ? 'last-name-error' : undefined} value={form.lastName} onChange={event => updateField('lastName', event.target.value)} />
                  {fieldErrors.lastName && <p id="last-name-error" className="field-error">{fieldErrors.lastName}</p>}
                </div>
                <div className="form-field">
                  <label htmlFor="birth-date">Birth date</label>
                  <input id="birth-date" name="dateOfBirth" type="date" autoComplete="bday" required aria-invalid={Boolean(fieldErrors.dateOfBirth)} aria-describedby={fieldErrors.dateOfBirth ? 'birth-date-error' : undefined} value={form.dateOfBirth} onChange={event => updateField('dateOfBirth', event.target.value)} />
                  {fieldErrors.dateOfBirth && <p id="birth-date-error" className="field-error">{fieldErrors.dateOfBirth}</p>}
                </div>
              </div>
              <button type="submit" disabled={submitting}>{submitting ? 'Creating...' : 'Create customer'}</button>
            </fieldset>
            <p className="creation-status" role="status">{submitting && !creationStatus ? 'Creating customer...' : creationStatus}</p>
            {creationError && <p className="error-message" role="alert">{creationError}</p>}
          </form>
        </section>
        <div className="page-heading">
          <div><h1>Customers</h1><p className="customer-count">{customers === null ? 'Customer records' : `${customers.length} ${customers.length === 1 ? 'customer' : 'customers'}`}</p></div>
          <button type="button" onClick={() => void refresh()} disabled={loading}>{loading ? 'Loading...' : error ? 'Retry' : 'Refresh'}</button>
        </div>
        <p className="status-message" role="status">{loading ? customers === null ? 'Loading customers...' : 'Refreshing customers...' : customers !== null ? 'Customer list loaded.' : ''}</p>
        {error && <p className="error-message" role="alert">{error}</p>}
        {customers !== null && customers.length === 0 && <div className="empty-state"><h2>No customers yet</h2><p>No customer records are available.</p></div>}
        {customers !== null && customers.length > 0 && (
          <div className="table-scroll" role="region" aria-label="Customer records" tabIndex={0} aria-busy={loading}>
            <table>
              <caption className="sr-only">Customers, newest first</caption>
              <thead><tr><th scope="col">ID</th><th scope="col">First name</th><th scope="col">Last name</th><th scope="col">Date of birth</th></tr></thead>
              <tbody>{customers.map(customer => <tr key={customer.id}>
                <td className="customer-id">{customer.id}</td><td>{customer.firstName}</td><td>{customer.lastName}</td>
                <td><time dateTime={customer.dateOfBirth}>{customer.dateOfBirth}</time></td>
              </tr>)}</tbody>
            </table>
          </div>
        )}
      </main>
    </>
  )
}

export default App