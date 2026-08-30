import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { eInvoiceApi, returnsApi, salesApi, type CheckoutRequest, type ReturnRequest } from '../api/sales'

export function useSales() {
  return useQuery({ queryKey: ['sales'], queryFn: salesApi.list })
}

export function useSale(id: number) {
  return useQuery({ queryKey: ['sales', id], queryFn: () => salesApi.get(id), enabled: Number.isFinite(id) })
}

export function useCheckout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CheckoutRequest) => salesApi.checkout(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sales'] }),
  })
}

export function useSaleReturns(saleId: number) {
  return useQuery({
    queryKey: ['sales', saleId, 'returns'],
    queryFn: () => returnsApi.list(saleId),
    enabled: Number.isFinite(saleId),
  })
}

export function useEInvoice(saleId: number | null) {
  return useQuery({
    queryKey: ['sales', saleId, 'e-invoice'],
    queryFn: () => eInvoiceApi.get(saleId as number),
    enabled: saleId !== null,
    refetchInterval: (query) => (query.state.data?.status === 'PENDING' ? 2000 : false),
  })
}

export function useCreateReturn(saleId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ReturnRequest) => returnsApi.create(saleId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sales', saleId, 'returns'] })
      queryClient.invalidateQueries({ queryKey: ['sales', saleId] })
    },
  })
}
