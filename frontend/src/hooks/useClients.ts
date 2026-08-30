import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clientsApi, type ClientRequest } from '../api/clients'

export function useClients() {
  return useQuery({ queryKey: ['clients'], queryFn: clientsApi.list })
}

export function useCreateClient() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ClientRequest) => clientsApi.create(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  })
}

export function useUpdateClient() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: ClientRequest }) => clientsApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  })
}

export function useDeleteClient() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => clientsApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  })
}
