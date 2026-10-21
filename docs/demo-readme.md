# Customer Management Demo

## Requirements

- Docker Desktop running with Linux containers, or Docker Engine with Docker Compose v2 or later.
- An x64 (AMD64) machine. These images target Linux AMD64. ARM64 machines such as Apple Silicon Macs may run them using emulation, but this bundle has not been verified on ARM64.
- Ports 8080 and 8081 available. Stop any source-built version of this demo first.

No Java, Node.js, database installation, corporate CA certificate, or dependency downloads are needed to run the supplied images. Docker must already be installed and running.

## Start

Extract the complete bundle into a directory. Open a terminal in that directory and run:

```powershell
docker load --input customer-management-images.tar
docker compose -f compose.prebuilt.yaml up
```

Wait for the backend to become healthy and the frontend to start. Keep the terminal running while using the demo.

- UI: http://localhost:8081
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API: http://localhost:8080/api/v1/customers

The Compose file uses only the loaded images. It does not build source code or pull images from a registry. If Docker reports a missing image, rerun the `docker load` command and check that it succeeds.

## Try It

1. Enter a first name, last name, and past birth date, then create a customer.
2. Confirm the customer appears in the list. Create another and confirm it appears first.
3. Refresh the browser and confirm the records remain while the backend runs.
4. Open Swagger UI to inspect and try the API.

## Stop

Press `Ctrl+C`, then remove the demo containers and network:

```powershell
docker compose -f compose.prebuilt.yaml down
```

H2 runs in memory inside the backend. All customer records are cleared when that container stops or restarts. The image archive and loaded images do not contain customer records.

If the default ports are occupied, set alternatives before starting (PowerShell):

```powershell
$env:DEMO_API_PORT = '18080'
$env:DEMO_UI_PORT = '18081'
docker compose -f compose.prebuilt.yaml up
```

Then use http://localhost:18081 for the UI and http://localhost:18080/swagger-ui/index.html for Swagger. These settings change only the host ports, not communication between the containers.

This is a local assessment demo, not a production deployment. Ports are bound to localhost. Authentication, durable storage, and production security controls are outside its scope.