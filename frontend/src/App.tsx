import { useEffect, useRef, useState } from 'react'
import { getCustomers } from './api'
import type { CustomerResponse } from './types'
import './App.css'

function App() {
  const [customers, setCustomers] = useState<CustomerResponse[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const activeRequest = useRef<AbortController | null>(null)

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

  async function refresh() {
    if (activeRequest.current) return
    const controller = new AbortController()
    activeRequest.current = controller
    setLoading(true)
    setError(null)
    try {
      const data = await getCustomers(controller.signal)
      if (!controller.signal.aborted) setCustomers(data)
    } catch {
      if (!controller.signal.aborted) setError('Customers could not be refreshed. Please try again.')
    } finally {
      if (!controller.signal.aborted) {
        activeRequest.current = null
        setLoading(false)
      }
    }
  }

  return (
    <>
      <a className="skip-link" href="#customers">Skip to customers</a>
      <header className="app-header"><div className="header-inner">
        <span className="app-name">Customer Management<span className="brand-dot" aria-hidden="true">.</span></span>
        <span className="header-label">Interview project</span>
      </div></header>
      <main id="customers">
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