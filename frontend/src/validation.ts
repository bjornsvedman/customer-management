import type { CreateCustomerRequest, CustomerFieldErrors } from './types'

export const customerFields: (keyof CreateCustomerRequest)[] = ['firstName', 'lastName', 'dateOfBirth']

export function validateCustomer(customer: CreateCustomerRequest, now = new Date()): CustomerFieldErrors {
  const errors: CustomerFieldErrors = {}
  for (const field of ['firstName', 'lastName'] as const) {
    const name = customer[field].trim()
    const label = field === 'firstName' ? 'First name' : 'Last name'
    if (!name) errors[field] = `${label} is required.`
    else if (name.length > 50) errors[field] = `${label} must be 50 characters or fewer.`
  }

  const birthDate = customer.dateOfBirth
  const parsedDate = new Date(`${birthDate}T00:00:00.000Z`)
  if (!birthDate) errors.dateOfBirth = 'Birth date is required.'
  else if (!/^\d{4}-\d{2}-\d{2}$/.test(birthDate) || Number.isNaN(parsedDate.getTime()) || parsedDate.toISOString().slice(0, 10) !== birthDate) {
    errors.dateOfBirth = 'Enter a valid birth date.'
  } else if (birthDate >= now.toISOString().slice(0, 10)) {
    errors.dateOfBirth = 'Birth date must be before today.'
  }
  return errors
}