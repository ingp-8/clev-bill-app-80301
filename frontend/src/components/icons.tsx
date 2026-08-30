interface IconProps {
  moduleCode: string
}

const paths: Record<string, string> = {
  BILLING: 'M3 3h2l2.4 12.4a2 2 0 0 0 2 1.6h8.2a2 2 0 0 0 2-1.6L21 8H6',
  MASTERS_CATEGORY: 'M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z',
  INVENTORY: 'M3 7l9-4 9 4-9 4-9-4zM3 7v10l9 4 9-4V7M12 11v10',
  REPORTS: 'M4 20V10M12 20V4M20 20v-7',
  PROPERTY_MGMT: 'M4 21V7l8-4 8 4v14M9 21v-6h6v6M4 21h16',
  CLIENT_MGMT: 'M17 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M10 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
  USER_MGMT: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
  ROLE_MGMT: 'M12 22s8-4 8-11V5l-8-3-8 3v6c0 7 8 11 8 11z',
}

const fallback = 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z'

export function NavIcon({ moduleCode }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d={paths[moduleCode] ?? fallback} />
    </svg>
  )
}
