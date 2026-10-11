import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { createCustomer, getCustomers } from './api'
import type { CreateCustomerRequest, CustomerResponse } from './types'
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
    submissionPending.current = true
    setSubmitting(true)
    setCreationError(null)
    setCreationStatus('')
    try {
      await createCustomer({ ...form, firstName: form.firstName.trim(), lastName: form.lastName.trim() })
    } catch {
      setCreationError('Creation could not be confirmed. Your entries have been kept. Check the customer list before trying again.')
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
          <form onSubmit={event => void submitCustomer(event)} aria-busy={submitting}>
            <fieldset disabled={submitting}>
              <legend className="sr-only">Customer details</legend>
              <div className="form-fields">
                <div className="form-field">
                  <label htmlFor="first-name">First name</label>
                  <input id="first-name" name="firstName" autoComplete="given-name" required value={form.firstName} onChange={event => setForm({ ...form, firstName: event.target.value })} />
                </div>
                <div className="form-field">
                  <label htmlFor="last-name">Last name</label>
                  <input id="last-name" name="lastName" autoComplete="family-name" required value={form.lastName} onChange={event => setForm({ ...form, lastName: event.target.value })} />
                </div>
                <div className="form-field">
                  <label htmlFor="birth-date">Birth date</label>
                  <input id="birth-date" name="dateOfBirth" type="date" autoComplete="bday" required value={form.dateOfBirth} onChange={event => setForm({ ...form, dateOfBirth: event.target.value })} />
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