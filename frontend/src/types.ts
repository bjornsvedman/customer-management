export interface CreateCustomerRequest {
  firstName: string
  lastName: string
  dateOfBirth: string
}

export interface CustomerResponse extends CreateCustomerRequest {
  id: number
}