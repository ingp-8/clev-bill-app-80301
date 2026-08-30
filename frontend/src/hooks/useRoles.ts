import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { permissionsApi, rolesApi, type RoleRequest } from '../api/roles'

export function useRoles() {
  return useQuery({ queryKey: ['roles'], queryFn: rolesApi.list })
}

export function usePermissions() {
  return useQuery({ queryKey: ['permissions'], queryFn: permissionsApi.list })
}

export function useCreateRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: RoleRequest) => rolesApi.create(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['roles'] }),
  })
}

export function useUpdateRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: RoleRequest }) => rolesApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['roles'] }),
  })
}

export function useDeleteRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => rolesApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['roles'] }),
  })
}
