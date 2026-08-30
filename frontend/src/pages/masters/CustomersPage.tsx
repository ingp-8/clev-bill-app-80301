import { ContactEntityPage } from '../../components/ContactEntityPage'
import { customerHooks } from '../../hooks/useMasters'

export function CustomersPage() {
  return <ContactEntityPage title="Customers" hooks={customerHooks} />
}
