import { SimpleNamedEntityPage } from '../../components/SimpleNamedEntityPage'
import { brandHooks } from '../../hooks/useMasters'

export function BrandsPage() {
  return <SimpleNamedEntityPage title="Brands" hooks={brandHooks} />
}
