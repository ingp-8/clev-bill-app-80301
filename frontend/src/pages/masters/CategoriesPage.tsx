import { SimpleNamedEntityPage } from '../../components/SimpleNamedEntityPage'
import { categoryHooks } from '../../hooks/useMasters'

export function CategoriesPage() {
  return <SimpleNamedEntityPage title="Categories" hooks={categoryHooks} />
}
