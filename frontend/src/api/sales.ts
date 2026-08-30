import { apiClient } from './client'
import type { MasterRef } from './masters'

export type PaymentMethod = 'CASH' | 'CARD' | 'UPI' | 'GIFT_CARD' | 'OTHER'

export interface SaleItemLine {
  id: number
  item: MasterRef
  sku: string
  name: string
  quantity: number
  unitPrice: number
  cgstAmount: number
  sgstAmount: number
  igstAmount: number
  lineSubtotal: number
  lineTotal: number
}

export interface SalePayment {
  id: number
  method: PaymentMethod
  amount: number
  createdAt: string
}

export interface Sale {
  id: number
  billNumber: string
  customer: MasterRef | null
  cashier: MasterRef
  items: SaleItemLine[]
  payments: SalePayment[]
  subtotalAmount: number
  taxAmount: number
  totalAmount: number
  createdAt: string
}

export interface CheckoutItemRequest {
  itemId: number
  quantity: number
}

export interface PaymentRequest {
  method: PaymentMethod
  amount: number
}

export interface CheckoutRequest {
  customerId: number | null
  items: CheckoutItemRequest[]
  payments: PaymentRequest[]
}

export const salesApi = {
  checkout: async (request: CheckoutRequest): Promise<Sale> => (await apiClient.post<Sale>('/v1/sales', request)).data,
  list: async (): Promise<Sale[]> => (await apiClient.get<Sale[]>('/v1/sales')).data,
  get: async (id: number): Promise<Sale> => (await apiClient.get<Sale>(`/v1/sales/${id}`)).data,
}

export interface ReturnItemLine {
  id: number
  saleItemId: number
  itemName: string
  quantity: number
  amount: number
}

export interface SaleReturn {
  id: number
  saleId: number
  reason: string | null
  totalAmount: number
  items: ReturnItemLine[]
  createdAt: string
}

export interface ReturnItemRequest {
  saleItemId: number
  quantity: number
}

export interface ReturnRequest {
  reason: string | null
  items: ReturnItemRequest[]
}

export type EInvoiceStatus = 'PENDING' | 'SUCCESS' | 'FAILED'

export interface EInvoice {
  id: number
  saleId: number
  status: EInvoiceStatus
  irn: string | null
  signedQrCode: string | null
  attempts: number
  lastError: string | null
  lastAttemptAt: string | null
}

export const eInvoiceApi = {
  get: async (saleId: number): Promise<EInvoice> => (await apiClient.get<EInvoice>(`/v1/sales/${saleId}/e-invoice`)).data,
}

export const returnsApi = {
  create: async (saleId: number, request: ReturnRequest): Promise<SaleReturn> =>
    (await apiClient.post<SaleReturn>(`/v1/sales/${saleId}/returns`, request)).data,
  list: async (saleId: number): Promise<SaleReturn[]> =>
    (await apiClient.get<SaleReturn[]>(`/v1/sales/${saleId}/returns`)).data,
}
