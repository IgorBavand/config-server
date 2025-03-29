# Spring Cloud Config Server & API-Test

Ce projet démontre la configuration d'un **Spring Cloud Config Server** pour fournir des configurations centralisées à d'autres applications Spring Boot.  
L'application **`api-test`** consomme ces configurations à partir d'un dépôt Git distant, permettant une gestion dynamique et efficace des environnements **sans avoir besoin de redémarrer l'application**.

[ 🇬🇧 Lire en Anglais](readme.md) | [ 🇧🇷 Ler em Português](readme.pt.md)

---

## 📌 Technologies Utilisées

- **Kotlin** + **Spring Boot**
- **Spring Cloud Config Server**
- **Spring Boot Config Client**
- **GitHub** comme dépôt de configurations
- **cURL / Postman** pour les tests
- **Docker** pour faciliter l'exécution des services

---

## 📂 Structure du Projet

Le projet contient **deux applications principales** :

### 1️⃣ **Config Server** ([Dépôt GitHub](https://github.com/igorbavand/config-server))

Ce serveur est responsable de fournir des configurations centralisées à d'autres applications.

### 2️⃣ **Config Server Environment** ([Dépôt GitHub](https://github.com/IgorBavand/config-server-environment))

Ce dépôt stocke les profils des applications à charger par le Config Server.

### 3️⃣ **API Test** ([Dépôt GitHub](https://github.com/igorbavand/api-test-config-server))

Cette application consomme les configurations fournies par le Config Server.

---

## 🔧 Configuration du Config Server

### **1️⃣ Configuration du `application.yml` du Config Server**

Le `Config Server` lit les fichiers de configuration à partir d'un dépôt Git spécifique. Le fichier `application.yml` de l'application **Config Server** est configuré comme suit :

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

## 📁 Organisation des Configurations dans le Dépôt Distant

Les configurations sont stockées dans le dépôt **[config-server-environment](https://github.com/igorbavand/config-server-environment)** avec la structure suivante :

```
config-server-environment/
│
├── api-test/
│   ├── application.yml  # Configuration par défaut
│   ├── dev/
│   │   ├── application.yml  # Configuration pour l'environnement DEV
│   ├── prod/
│   │   ├── application.yml  # Configuration pour l'environnement PROD
```

Exemple de configuration de l'environnement `dev` :

```yaml
server:
  port: 8070

mensagem:
  bemvindo: "Bienvenue dans l'environnement de développement de l'API Test !"
```

---

## 🐳 Exécution du Config Server avec Docker

Nous pouvons maintenant exécuter le **Config Server** en utilisant Docker pour faciliter l'exécution.

### **1️⃣ Création du Dockerfile**

Dans le répertoire du `Config Server`, créez un fichier nommé `Dockerfile` et ajoutez le contenu suivant :

```dockerfile
# Utilisation de l'image officielle OpenJDK 17
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR généré dans le conteneur
COPY target/config-server.jar app.jar

# Définir la variable d'environnement pour le profil actif (peut être modifié via argument)
ENV SPRING_PROFILES_ACTIVE=prod

# Exposer le port du serveur
EXPOSE 8888

# Commande pour exécuter l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **2️⃣ Construction de l'Image Docker**

Avant de créer l'image, générez le JAR de l'application :

```bash
./mvnw clean package -DskipTests
```

Créez maintenant l'image Docker :

```bash
docker build -t