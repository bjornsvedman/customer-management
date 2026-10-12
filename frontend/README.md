# Customer Management Frontend

React 19 and TypeScript UI for creating customers and displaying the newest-first customer list. Built with Vite.

See the [root README](../README.md) for prerequisites, backend startup, and design decisions, and the [specification](../SPEC.md) for the API and validation contract.

## Run Locally

Start the backend on `http://localhost:8080`, then run these commands from this directory:

```powershell
npm ci
npm run dev
```

Open the URL printed by Vite. The development server proxies `/api` requests to the backend.

## Verify

```powershell
npm run lint
npm run build
npm test
```

Tests use Vitest, Testing Library, and jsdom. Form tests mock API calls; live end-to-end verification requires the backend to be running.
  Start the backend on `http://localhost:8080`, then run these commands from this directory:

