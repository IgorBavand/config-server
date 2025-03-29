# Spring Cloud Config Server & API-Test

This project demonstrates the setup of a **Spring Cloud Config Server** to provide centralized configurations for other Spring Boot applications.  
The **`api-test`** application consumes these configurations from a remote Git repository, allowing dynamic and efficient management of environments **without needing to restart the application**.

[ 🇧🇷 Read in Portuguese](readme-pt.md) | [ 🇫🇷 Lire en Français](readme-fr.md)

---

## 📌 Technologies Used

- **Kotlin** + **Spring Boot**
- **Spring Cloud Config Server**
- **Spring Boot Config Client**
- **GitHub** as the configuration repository
- **cURL / Postman** for testing
- **Docker** to facilitate the execution of services

---

## 📂 Project Structure

The project contains **two main applications**:

### 1️⃣ **Config Server** ([GitHub Repository](https://github.com/igorbavand/config-server))

This server is responsible for providing centralized configurations to other applications.

### 2️⃣ **Config Server Environment** ([GitHub Repository](https://github.com/IgorBavand/config-server-environment))

This repository stores the application profiles to be loaded by the Config Server.

### 3️⃣ **API Test** ([GitHub Repository](https://github.com/igorbavand/api-test-config-server))

This application consumes the configurations provided by the Config Server.

---

## 🔧 Configuring the Config Server

### **1️⃣ Configuring the `application.yml` of the Config Server**

The `Config Server` reads configuration files from a specific Git repository. The `application.yml` file of the **Config Server** application is configured as follows:

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/igorbavand/config-server-environment.git
          username: ${GITHUB_USER}
          password: ${GITHUB_PASSWORD}
          search-paths: '{application}/{profile}'
          clone-on-start: true
          defaultLabel: main
  output:
    ansi:
      enabled: ALWAYS

api-token: ${TOKEN-API}

encrypt:
  key: ${ENCRYPT_KEY}
```

---

## 📁 Organizing the Configurations in the Remote Repository

The configurations are stored in the **[config-server-environment](https://github.com/igorbavand/config-server-environment)** repository with the following structure:

```
config-server-environment/
│
├── api-test/
│   ├── application.yml  # Default configuration
│   ├── dev/
│   │   ├── application.yml  # Configuration for DEV environment
│   ├── prod/
│   │   ├── application.yml  # Configuration for PROD environment
```

Example of `dev` environment configuration:

```yaml
server:
  port: 8070

mensagem:
  bemvindo: "Welcome to the development environment of API Test!"
```

---

## 🐳 Running the Config Server with Docker

Now, we can run the **Config Server** using Docker to facilitate execution.

### **1️⃣ Creating the Dockerfile**

In the `Config Server` directory, create a file named `Dockerfile` and add the following content:

```dockerfile
# Using the official OpenJDK 17 image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the generated JAR into the container
COPY target/config-server.jar app.jar

# Set the environment variable for the active profile (can be changed via argument)
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the server port
EXPOSE 8888

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **2️⃣ Building the Docker Image**

Before creating the image, generate the application JAR:

```bash
./mvnw clean package -DskipTests
```

Now, create the Docker image:

```bash
docker build -t config-server .
```

### **3️⃣ Running the Container**

Now, run the `Config Server` in Docker:

```bash
docker run -d --name config-server -p 8888:8888   -e SPRING_PROFILES_ACTIVE=prod   -e GITHUB_USER=your_username   -e GITHUB_PASSWORD=your_password   -e TOKEN-API=your_api_token   -e ENCRYPT_KEY=your_encryption_key   config-server
```

### **4️⃣ Testing if It's Working**

Check if the container is running:

```bash
docker ps
```

Now, query the configurations exposed by the Config Server:

```bash
curl -H "x-config-token: your_api_token" http://localhost:8888/api-test/prod
```

---

## 📡 Configuring the API-Test Application

The `api-test` application consumes the configurations from the Config Server automatically.  
To do this, the `bootstrap.yml` is configured as follows:

```yaml
spring:
  application:
    name: api-test
  config:
    import: configserver:${CONFIG_SERVER_URL}
  cloud:
    config:
      headers:
        x-config-token: ${TOKEN-API}
      profile: prod
```

---

## 📡 Testing the Configurations

### **1️⃣ Verifying the Configurations via `cURL`**

To retrieve the configurations of the `prod` profile, run:

```bash
curl -H "x-config-token: your_token" http://localhost:8888/api-test/prod
```

Expected output:

```json
{
  "name": "api-test",
  "profiles": ["prod"],
  "propertySources": [
    {
      "name": "https://github.com/igorbavand/config-server-environment.git/api-test/prod/application.yml",
      "source": {
        "server.port": 8070,
        "mensagem.bemvindo": "Welcome to the production environment of API Test!"
      }
    }
  ]
}
```

### **2️⃣ Updating Configurations Without Restarting the Application**

If any configuration is changed in the repository, simply update the `API-Test` with:

```bash
curl -X POST http://localhost:8070/actuator/refresh
```

### **3️⃣ Starting the API-Test**

Run the `api-test` with:

```bash
./mvnw spring-boot:run
```

And access the configuration via:

```bash
curl http://localhost:8070/config
```

Expected output:

```bash
Message from Config Server: Welcome to the production environment of API Test!
```

---

## 🚀 Conclusion

With this setup, the `api-test` application can **dynamically fetch its configurations** from the `Config Server`, which in turn **stores the configuration files** in a remote GitHub repository.

Now, **configurations can be updated without needing to restart the application**, ensuring greater efficiency and flexibility. Additionally, the use of **Docker** allows running the `Config Server` in an isolated and standardized manner in any environment.