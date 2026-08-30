import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { usersApi, type UserRequest } from '../api/users'

export function useUsers() {
  return useQuery({ queryKey: ['users'], queryFn: usersApi.list })
}

export function useCreateUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: UserRequest) => usersApi.create(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  })
}

export function useUpdateUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UserRequest }) => usersApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  })
}

export function useDeleteUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => usersApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  })
}
