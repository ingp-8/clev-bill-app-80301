import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { propertiesApi, type PropertyRequest } from '../api/properties'

export function usePropertiesByClient(clientId: number | null) {
  return useQuery({
    queryKey: ['properties', clientId],
    queryFn: () => propertiesApi.listByClient(clientId as number),
    enabled: clientId !== null,
  })
}

export function useCreateProperty() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ clientId, request }: { clientId: number; request: PropertyRequest }) =>
      propertiesApi.create(clientId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties'] })
      queryClient.invalidateQueries({ queryKey: ['me', 'properties'] })
    },
  })
}

export function useUpdateProperty() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: PropertyRequest }) => propertiesApi.update(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties'] })
      queryClient.invalidateQueries({ queryKey: ['me', 'properties'] })
    },
  })
}

export function useDeleteProperty() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => propertiesApi.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['properties'] })
      queryClient.invalidateQueries({ queryKey: ['me', 'properties'] })
    },
  })
}
