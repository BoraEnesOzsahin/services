# GPU Monitoring Service

This service monitors GPU-enabled compute nodes.

## How to Run

There are two ways to run this application: using Docker (recommended for ease of setup) or by running all services locally on a Linux machine.

---

### Option 1: Run with Docker (Recommended)

This is the easiest way to get started, as it automatically sets up the required database and service discovery agent.

1.  **Prerequisites:**
    *   **Java 21** or later.
    *   **Docker** and **Docker Compose**.

2.  **Start Dependent Services:**
    The included `docker-compose.yml` file will start PostgreSQL and Consul.

    ```bash
    docker-compose up -d
    ```

3.  **Build and Run:**
    This project uses the Maven wrapper (`mvnw`), so you don't need to install Maven.

    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```

---

### Option 2: Run Locally on Linux (Without Docker)

If you prefer not to use Docker, you can run the service and its dependencies directly on your Linux machine.

1.  **Prerequisites:**
    *   **Java 21** or later.
    *   **PostgreSQL**.
    *   **Consul**.

2.  **Install and Configure Dependencies:**

    *   **PostgreSQL:**
        Install PostgreSQL using your distribution's package manager (e.g., `sudo apt-get install postgresql`). Then, create the database and user for the application.

        ```sql
        CREATE DATABASE gpu_monitoring_db;
        CREATE USER postgres WITH PASSWORD 'postgres';
        GRANT ALL PRIVILEGES ON DATABASE gpu_monitoring_db TO postgres;
        ```

    *   **Consul:**
        Download and install Consul from the [official website](https://www.consul.io/downloads). For local development, you can run it in agent mode.

        ```bash
        consul agent -dev
        ```

3.  **Configure the Application:**
    The default configuration in `src/main/resources/application.yaml` is designed for Docker. To run locally, you need to override the database and Consul connection settings. You can do this by creating a `.env` file in the project root.

    Create a `.env` file with the following content:
    ```
    DB_URL=jdbc:postgresql://localhost:5432/gpu_monitoring_db
    DB_USERNAME=postgres
    DB_PASSWORD=postgres
    CONSUL_HOST=localhost
    CONSUL_PORT=8500
    ```

4.  **Build and Run the Application:**
    Use the Maven wrapper to build and run the service.

    ```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
    ```

The service will start on the port specified in `application.yaml` (default is 8080) and connect to your local PostgreSQL and Consul instances.
