import { ContactEntityPage } from '../../components/ContactEntityPage'
import { supplierHooks } from '../../hooks/useMasters'

export function SuppliersPage() {
  return <ContactEntityPage title="Suppliers" hooks={supplierHooks} />
}
