# Urban Flagship Frontend

Production-grade React frontend for the Urban Flagship urban intelligence platform.

## Tech Stack

- React 18 + TypeScript
- Vite
- TailwindCSS + shadcn/ui
- React Router v6
- Axios with JWT interceptors
- TanStack Query for data fetching
- Recharts for analytics

## Project Structure

```
src/
├── app/                 # App shell and routing
├── components/
│   ├── ui/             # shadcn/ui components
│   └── common/         # Reusable components (Header, Sidebar, etc.)
├── features/           # Feature modules
│   ├── auth/
│   ├── dashboard/
│   ├── incidents/
│   ├── analytics/
│   ├── recommendations/
│   └── districts/
├── services/           # API clients (auth, api)
├── context/            # React Context (auth state)
├── hooks/              # Custom hooks
├── layouts/            # Page layouts (MainLayout)
├── pages/              # Page components
├── types/              # TypeScript types
└── lib/                # Utilities and helpers
```

## Setup

```bash
cd frontend
npm install
npm run dev
```

## Building

```bash
npm run build
npm run preview
```

## Environment Variables

Create a `.env.local` file:

```
VITE_API_URL=http://localhost:8080/api
```

## Features

- ✅ Production auth flow (JWT + refresh tokens)
- ✅ Protected routes with role-based access
- ✅ API client with automatic token refresh
- ✅ Modern UI components
- ✅ Responsive design
- ✅ Dark mode support
- ✅ Error handling and loading states
