export interface CreateCustomerRequest {
  firstName: string
  lastName: string
  dateOfBirth: string
}

export interface CustomerResponse extends CreateCustomerRequest {
  id: number
}

export type CustomerFieldErrors = Partial<Record<keyof CreateCustomerRequest, string>>

export interface FieldValidationError {
  field: string
  message: string
}

export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errors?: FieldValidationError[]
}