import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchSystemConfig, updateSystemConfig, type SystemConfigRequest } from '../api/configApi'

export function useSystemConfig() {
  return useQuery({
    queryKey: ['system-config'],
    queryFn: fetchSystemConfig,
    staleTime: Infinity,
  })
}

export function useUpdateSystemConfig() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: SystemConfigRequest) => updateSystemConfig(request),
    onSuccess: (data) => queryClient.setQueryData(['system-config'], data),
  })
}
