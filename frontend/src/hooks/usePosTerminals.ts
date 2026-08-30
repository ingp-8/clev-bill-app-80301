import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { posTerminalsApi, type PosTerminalRequest } from '../api/posTerminals'

export function usePosTerminalsByProperty(propertyId: number) {
  return useQuery({
    queryKey: ['pos', propertyId],
    queryFn: () => posTerminalsApi.listByProperty(propertyId),
    enabled: Number.isFinite(propertyId),
  })
}

export function useCreatePosTerminal(propertyId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: PosTerminalRequest) => posTerminalsApi.create(propertyId, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pos', propertyId] }),
  })
}

export function useUpdatePosTerminal(propertyId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: PosTerminalRequest }) => posTerminalsApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pos', propertyId] }),
  })
}

export function useDeletePosTerminal(propertyId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => posTerminalsApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pos', propertyId] }),
  })
}
