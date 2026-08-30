import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  brandsApi,
  categoriesApi,
  customersApi,
  itemsApi,
  priceListItemsApi,
  priceListsApi,
  suppliersApi,
  taxRatesApi,
  type PriceListItemRequest,
} from '../api/masters'

function createPropertyScopedHooks<TEntity, TRequest>(
  queryKey: string,
  api: {
    listByProperty: (propertyId: number) => Promise<TEntity[]>
    create: (propertyId: number, request: TRequest) => Promise<TEntity>
    update: (id: number, request: TRequest) => Promise<TEntity>
    remove: (id: number) => Promise<void>
  },
) {
  function useListByProperty(propertyId: number) {
    return useQuery({
      queryKey: [queryKey, propertyId],
      queryFn: () => api.listByProperty(propertyId),
      enabled: Number.isFinite(propertyId),
    })
  }

  function useCreateInProperty(propertyId: number) {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: (request: TRequest) => api.create(propertyId, request),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [queryKey, propertyId] }),
    })
  }

  function useUpdate(propertyId: number) {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: ({ id, request }: { id: number; request: TRequest }) => api.update(id, request),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [queryKey, propertyId] }),
    })
  }

  function useDelete(propertyId: number) {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: (id: number) => api.remove(id),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [queryKey, propertyId] }),
    })
  }

  return { useListByProperty, useCreateInProperty, useUpdate, useDelete }
}

export const categoryHooks = createPropertyScopedHooks('categories', categoriesApi)
export const brandHooks = createPropertyScopedHooks('brands', brandsApi)
export const taxRateHooks = createPropertyScopedHooks('tax-rates', taxRatesApi)
export const itemHooks = createPropertyScopedHooks('items', itemsApi)
export const priceListHooks = createPropertyScopedHooks('price-lists', priceListsApi)

function createClientScopedHooks<TEntity, TRequest>(
  queryKey: string,
  api: {
    listByClient: (clientId: number) => Promise<TEntity[]>
    create: (clientId: number, request: TRequest) => Promise<TEntity>
    update: (id: number, request: TRequest) => Promise<TEntity>
    remove: (id: number) => Promise<void>
  },
) {
  function useListByClient(clientId: number) {
    return useQuery({
      queryKey: [queryKey, clientId],
      queryFn: () => api.listByClient(clientId),
      enabled: Number.isFinite(clientId),
    })
  }

  function useCreateForClient(clientId: number) {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: (request: TRequest) => api.create(clientId, request),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [queryKey, clientId] }),
    })
  }

  function useUpdate(clientId: number) {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: ({ id, request }: { id: number; request: TRequest }) => api.update(id, request),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [queryKey, clientId] }),
    })
  }

  function useDelete(clientId: number) {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: (id: number) => api.remove(id),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [queryKey, clientId] }),
    })
  }

  return { useListByClient, useCreateForClient, useUpdate, useDelete }
}

export const customerHooks = createClientScopedHooks('customers', customersApi)
export const supplierHooks = createClientScopedHooks('suppliers', suppliersApi)

export function usePriceListItems(priceListId: number) {
  return useQuery({
    queryKey: ['price-lists', priceListId, 'items'],
    queryFn: () => priceListItemsApi.list(priceListId),
    enabled: Number.isFinite(priceListId),
  })
}

export function useCreatePriceListItem(priceListId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: PriceListItemRequest) => priceListItemsApi.create(priceListId, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['price-lists', priceListId, 'items'] }),
  })
}

export function useDeletePriceListItem(priceListId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => priceListItemsApi.remove(priceListId, id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['price-lists', priceListId, 'items'] }),
  })
}
