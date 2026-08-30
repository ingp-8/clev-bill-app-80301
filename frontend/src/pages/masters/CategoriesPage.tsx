import { SimpleNamedEntityPage } from '../../components/SimpleNamedEntityPage'
import { categoryHooks } from '../../hooks/useMasters'
import { useProperty } from '../../property/PropertyContext'

export function CategoriesPage() {
  const { activeProperty } = useProperty()
  const propertyId = activeProperty!.id
  return (
    <SimpleNamedEntityPage
      title="Categories"
      hooks={{
        useList: () => categoryHooks.useListByProperty(propertyId),
        useCreate: () => categoryHooks.useCreateInProperty(propertyId),
        useUpdate: () => categoryHooks.useUpdate(propertyId),
        useDelete: () => categoryHooks.useDelete(propertyId),
      }}
    />
  )
}
