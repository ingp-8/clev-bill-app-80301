import { PartyEntityPage } from '../../components/PartyEntityPage'
import { supplierHooks } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'

export function SuppliersPage() {
  const { activeProperty } = useProperty()
  const clientId = activeProperty!.clientId
  return (
    <PartyEntityPage
      title="Suppliers"
      hooks={{
        useList: () => supplierHooks.useListByClient(clientId),
        useCreate: () => supplierHooks.useCreateForClient(clientId),
        useUpdate: () => supplierHooks.useUpdate(clientId),
        useDelete: () => supplierHooks.useDelete(clientId),
      }}
    />
  )
}
