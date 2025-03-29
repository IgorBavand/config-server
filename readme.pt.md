# Spring Cloud Config Server & API-Test

Este projeto demonstra a configuração de um **Spring Cloud Config Server** para fornecer configurações centralizadas para outras aplicações Spring Boot.  
A aplicação **`api-test`** consome essas configurações de um repositório Git remoto, permitindo um gerenciamento dinâmico e eficiente dos ambientes **sem necessidade de reiniciar a aplicação**.

[ 🇬🇧 Ler em Inglês](readme.md) | [ 🇫🇷 Lire en Français](readme-fr.md)

---

## 📌 Tecnologias Utilizadas

- **Kotlin** + **Spring Boot**
- **Spring Cloud Config Server**
- **Spring Boot Config Client**
- **GitHub** como repositório de configurações
- **cURL / Postman** para testes
- **Docker** para facilitar a execução dos serviços

---

## 📂 Estrutura do Projeto

O projeto contém **duas aplicações principais**:

### 1️⃣ **Config Server** ([Repositório no GitHub](https://github.com/igorbavand/config-server))

Este servidor é responsável por fornecer as configurações centralizadas para outras aplicações.

### 2️⃣ **Config Server Environment** ([Repositório no GitHub](https://github.com/IgorBavand/config-server-environment))

Este repositório é onde estão armazenados os perfils das aplicações para serem carregados pelo Config Server.

### 3️⃣ **API Test** ([Repositório no GitHub](https://github.com/igorbavand/api-test-config-server))

Esta aplicação consome as configurações disponibilizadas pelo Config Server.

---

## 🔧 Configuração do Config Server

### **1️⃣ Configurando o `application.yml` do Config Server**

O `Config Server` lê os arquivos de configuração de um repositório Git específico. O arquivo `application.yml` da aplicação **Config Server** está configurado da seguinte forma:

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

## 📁 Organização das Configurações no Repositório Remoto

As configurações estão armazenadas no repositório **[config-server-environment](https://github.com/igorbavand/config-server-environment)** com a seguinte estrutura:

```
config-server-environment/
│
├── api-test/
│   ├── application.yml  # Configuração padrão
│   ├── dev/
│   │   ├── application.yml  # Configuração para ambiente DEV
│   ├── prod/
│   │   ├── application.yml  # Configuração para ambiente PROD
```

Exemplo de configuração do ambiente `dev`:

```yaml
server:
  port: 8070

mensagem:
  bemvindo: "Bem-vindo ao ambiente de desenvolvimento da API Test!"
```

---

## 🐳 Executando o Config Server com Docker

Agora, podemos rodar o **Config Server** utilizando Docker para facilitar a execução.

### **1️⃣ Criando o Dockerfile**

No diretório do `Config Server`, crie um arquivo chamado `Dockerfile` e adicione o seguinte conteúdo:

```dockerfile
# Usando a imagem oficial do OpenJDK 17
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR gerado para dentro do container
COPY target/config-server.jar app.jar

# Define a variável de ambiente para o perfil ativo (pode ser alterado via argumento)
ENV SPRING_PROFILES_ACTIVE=prod

# Expondo a porta do servidor
EXPOSE 8888

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **2️⃣ Construindo a Imagem Docker**

Antes de criar a imagem, gere o JAR da aplicação:

```bash
./mvnw clean package -DskipTests
```

Agora, crie a imagem Docker:

```bash
docker build -t config-server .
```

### **3️⃣ Executando o Container**

Agora, execute o `Config Server` no Docker:

```bash
docker run -d --name config-server -p 8888:8888   -e SPRING_PROFILES_ACTIVE=prod   -e GITHUB_USER=seu_usuario   -e GITHUB_PASSWORD=sua_senha   -e TOKEN-API=seu_token_api   -e ENCRYPT_KEY=sua_chave_de_criptografia   config-server
```

### **4️⃣ Testando se Está Funcionando**

Verifique se o container está rodando:

```bash
docker ps
```

Agora, consulte as configurações expostas pelo Config Server:

```bash
curl -H "x-config-token: seu_token_api" http://localhost:8888/api-test/prod
```

---

## 📡 Configuração da Aplicação API-Test

A aplicação `api-test` consome as configurações do Config Server automaticamente.  
Para isso, o `bootstrap.yml` foi configurado conforme abaixo:

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

## 📡 Testando as Configurações

### **1️⃣ Verificando as Configurações via `cURL`**

Para recuperar as configurações do profile `prod`, execute:

```bash
curl -H "x-config-token: meutoken" http://localhost:8888/api-test/prod
```

Saída esperada:

```json
{
  "name": "api-test",
  "profiles": ["prod"],
  "propertySources": [
    {
      "name": "https://github.com/igorbavand/config-server-environment.git/api-test/prod/application.yml",
      "source": {
        "server.port": 8070,
        "mensagem.bemvindo": "Bem-vindo ao ambiente de produção da API Test!"
      }
    }
  ]
}
```

### **2️⃣ Atualizando Configurações Sem Reiniciar a Aplicação**

Se alguma configuração for alterada no repositório, basta atualizar a `API-Test` com:

```bash
curl -X POST http://localhost:8070/actuator/refresh
```

### **3️⃣ Iniciando a API-Test**

Execute a `api-test` com:

```bash
./mvnw spring-boot:run
```

E acesse a configuração via:

```bash
curl http://localhost:8070/config
```

Saída esperada:

```bash
Mensagem do Config Server: Bem-vindo ao ambiente de produção da API Test!
```

---

## 🚀 Conclusão

Com essa configuração, a aplicação `api-test` pode **buscar suas configurações dinamicamente** do `Config Server`, que por sua vez **armazena os arquivos de configuração** em um repositório remoto no GitHub.

Agora, **as configurações podem ser atualizadas sem necessidade de reiniciar a aplicação**, garantindo maior eficiência e flexibilidade. Além disso, o uso do **Docker** permite rodar o `Config Server` de forma isolada e padronizada em qualquer ambiente.