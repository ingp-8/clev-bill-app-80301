import { SimpleNamedEntityPage } from '../../components/SimpleNamedEntityPage'
import { brandHooks } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'

export function BrandsPage() {
  const { activeProperty } = useProperty()
  const propertyId = activeProperty!.id
  return (
    <SimpleNamedEntityPage
      title="Brands"
      hooks={{
        useList: () => brandHooks.useListByProperty(propertyId),
        useCreate: () => brandHooks.useCreateInProperty(propertyId),
        useUpdate: () => brandHooks.useUpdate(propertyId),
        useDelete: () => brandHooks.useDelete(propertyId),
      }}
    />
  )
}
