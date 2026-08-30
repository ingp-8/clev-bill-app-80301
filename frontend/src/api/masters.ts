import { apiClient } from './client'

export interface MasterRef {
  id: number
  name: string
}

export interface Category {
  id: number
  propertyId: number
  clientId: number
  name: string
  active: boolean
  createdAt: string
  updatedAt: string
}
export interface CategoryRequest {
  name: string
  active: boolean
}

export interface Brand {
  id: number
  propertyId: number
  clientId: number
  name: string
  active: boolean
  createdAt: string
  updatedAt: string
}
export interface BrandRequest {
  name: string
  active: boolean
}

export interface TaxRate {
  id: number
  propertyId: number
  clientId: number
  name: string
  cgstRate: number
  sgstRate: number
  igstRate: number
  active: boolean
  createdAt: string
  updatedAt: string
}
export interface TaxRateRequest {
  name: string
  cgstRate: number
  sgstRate: number
  igstRate: number
  active: boolean
}

export interface Customer {
  id: number
  clientId: number
  name: string
  phone: string | null
  email: string | null
  gstin: string | null
  address: string | null
  active: boolean
  propertyIds: number[]
  createdAt: string
  updatedAt: string
}
export type CustomerRequest = Omit<Customer, 'id' | 'clientId' | 'createdAt' | 'updatedAt'>

export interface Supplier {
  id: number
  clientId: number
  name: string
  phone: string | null
  email: string | null
  gstin: string | null
  address: string | null
  active: boolean
  propertyIds: number[]
  createdAt: string
  updatedAt: string
}
export type SupplierRequest = Omit<Supplier, 'id' | 'clientId' | 'createdAt' | 'updatedAt'>

export type ItemUnit = 'PCS' | 'BOX' | 'KG' | 'GM' | 'LTR' | 'ML'

export interface Item {
  id: number
  propertyId: number
  clientId: number
  sku: string
  barcode: string | null
  name: string
  category: MasterRef | null
  brand: MasterRef | null
  taxRate: TaxRate
  hsnCode: string
  unit: ItemUnit
  sellingPrice: number
  costPrice: number | null
  active: boolean
  createdAt: string
  updatedAt: string
}
export interface ItemRequest {
  sku: string
  barcode: string | null
  name: string
  categoryId: number | null
  brandId: number | null
  taxRateId: number
  hsnCode: string
  unit: ItemUnit
  sellingPrice: number
  costPrice: number | null
  active: boolean
}

export interface PriceList {
  id: number
  propertyId: number
  clientId: number
  name: string
  isDefault: boolean
  active: boolean
  createdAt: string
  updatedAt: string
}
export interface PriceListRequest {
  name: string
  isDefault: boolean
  active: boolean
}

export interface PriceListItem {
  id: number
  priceListId: number
  item: MasterRef
  price: number
  createdAt: string
  updatedAt: string
}
export interface PriceListItemRequest {
  itemId: number
  price: number
}

function propertyScopedCrud<TEntity, TRequest>(resource: string) {
  return {
    listByProperty: async (propertyId: number): Promise<TEntity[]> =>
      (await apiClient.get<TEntity[]>(`/v1/properties/${propertyId}/${resource}`)).data,
    get: async (id: number): Promise<TEntity> => (await apiClient.get<TEntity>(`/v1/${resource}/${id}`)).data,
    create: async (propertyId: number, request: TRequest): Promise<TEntity> =>
      (await apiClient.post<TEntity>(`/v1/properties/${propertyId}/${resource}`, request)).data,
    update: async (id: number, request: TRequest): Promise<TEntity> =>
      (await apiClient.put<TEntity>(`/v1/${resource}/${id}`, request)).data,
    remove: async (id: number): Promise<void> => {
      await apiClient.delete(`/v1/${resource}/${id}`)
    },
  }
}

function clientScopedCrud<TEntity, TRequest>(resource: string) {
  return {
    listByClient: async (clientId: number): Promise<TEntity[]> =>
      (await apiClient.get<TEntity[]>(`/v1/clients/${clientId}/${resource}`)).data,
    get: async (id: number): Promise<TEntity> => (await apiClient.get<TEntity>(`/v1/${resource}/${id}`)).data,
    create: async (clientId: number, request: TRequest): Promise<TEntity> =>
      (await apiClient.post<TEntity>(`/v1/clients/${clientId}/${resource}`, request)).data,
    update: async (id: number, request: TRequest): Promise<TEntity> =>
      (await apiClient.put<TEntity>(`/v1/${resource}/${id}`, request)).data,
    remove: async (id: number): Promise<void> => {
      await apiClient.delete(`/v1/${resource}/${id}`)
    },
  }
}

export const categoriesApi = propertyScopedCrud<Category, CategoryRequest>('categories')
export const brandsApi = propertyScopedCrud<Brand, BrandRequest>('brands')
export const taxRatesApi = propertyScopedCrud<TaxRate, TaxRateRequest>('tax-rates')
export const itemsApi = propertyScopedCrud<Item, ItemRequest>('items')
export const priceListsApi = propertyScopedCrud<PriceList, PriceListRequest>('price-lists')

export const customersApi = clientScopedCrud<Customer, CustomerRequest>('customers')
export const suppliersApi = clientScopedCrud<Supplier, SupplierRequest>('suppliers')

export const priceListItemsApi = {
  list: async (priceListId: number): Promise<PriceListItem[]> =>
    (await apiClient.get<PriceListItem[]>(`/v1/masters/price-lists/${priceListId}/items`)).data,
  create: async (priceListId: number, request: PriceListItemRequest): Promise<PriceListItem> =>
    (await apiClient.post<PriceListItem>(`/v1/masters/price-lists/${priceListId}/items`, request)).data,
  update: async (priceListId: number, id: number, request: PriceListItemRequest): Promise<PriceListItem> =>
    (await apiClient.put<PriceListItem>(`/v1/masters/price-lists/${priceListId}/items/${id}`, request)).data,
  remove: async (priceListId: number, id: number): Promise<void> => {
    await apiClient.delete(`/v1/masters/price-lists/${priceListId}/items/${id}`)
  },
}
