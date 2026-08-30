import { PartyEntityPage } from '../../components/PartyEntityPage'
import { customerHooks } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'

export function CustomersPage() {
  const { activeProperty } = useProperty()
  const clientId = activeProperty!.clientId
  return (
    <PartyEntityPage
      title="Customers"
      hooks={{
        useList: () => customerHooks.useListByClient(clientId),
        useCreate: () => customerHooks.useCreateForClient(clientId),
        useUpdate: () => customerHooks.useUpdate(clientId),
        useDelete: () => customerHooks.useDelete(clientId),
      }}
    />
  )
}
