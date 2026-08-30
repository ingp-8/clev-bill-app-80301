import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { inventoryApi, type InventoryAdjustmentRequest } from '../api/inventory'

export function useInventoryList() {
  return useQuery({ queryKey: ['inventory'], queryFn: inventoryApi.list })
}

export function useInventoryTransactions(itemId: number) {
  return useQuery({
    queryKey: ['inventory', itemId, 'transactions'],
    queryFn: () => inventoryApi.transactions(itemId),
    enabled: Number.isFinite(itemId),
  })
}

export function useAdjustInventory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: InventoryAdjustmentRequest) => inventoryApi.adjust(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['inventory'] }),
  })
}
