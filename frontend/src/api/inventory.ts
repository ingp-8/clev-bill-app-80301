import { apiClient } from './client'

export interface InventoryLevel {
  itemId: number
  sku: string
  itemName: string
  quantity: number
}

export type InventoryChangeReason = 'SALE' | 'RETURN' | 'ADJUSTMENT'

export interface InventoryTransaction {
  id: number
  itemId: number
  changeQuantity: number
  reason: InventoryChangeReason
  referenceId: number | null
  note: string | null
  createdByName: string | null
  createdAt: string
}

export interface InventoryAdjustmentRequest {
  itemId: number
  quantityDelta: number
  note: string | null
}

export const inventoryApi = {
  list: async (): Promise<InventoryLevel[]> => (await apiClient.get<InventoryLevel[]>('/v1/inventory')).data,
  get: async (itemId: number): Promise<InventoryLevel> =>
    (await apiClient.get<InventoryLevel>(`/v1/inventory/${itemId}`)).data,
  transactions: async (itemId: number): Promise<InventoryTransaction[]> =>
    (await apiClient.get<InventoryTransaction[]>(`/v1/inventory/${itemId}/transactions`)).data,
  adjust: async (request: InventoryAdjustmentRequest): Promise<InventoryLevel> =>
    (await apiClient.post<InventoryLevel>('/v1/inventory/adjustments', request)).data,
}
