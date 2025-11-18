# 🧠 AgileSprintPlus — Backend (Spring Boot)

## 📋 Description
**AgileSprintPlus Backend** est une API RESTful développée avec **Spring Boot 3.5+**, servant de moteur principal pour la plateforme de gestion Agile.  
Elle gère les entités principales : **Users, Tasks, Sprints** avec authentification **JWT**, rôles et permissions granulaires, ainsi qu’un système de notifications email et d’analytics (velocity, gamification…).

## ⚙️ Stack Technique
| Technologie | Version | Description |
|--------------|------|--------------|
| Java | 17+  | Langage principal |
| Spring Boot | 3.5.x | Framework principal |
| Spring Security + JWT | —    | Authentification & rôles |
| MapStruct | 1.6.x | Mapping DTO ↔ Entities |
| PostgreSQL | 17   | Base de données |
| Lombok | —    | Réduction du boilerplate |
| Hibernate | —    | ORM |
| Swagger / OpenAPI | 3    | Documentation interactive |
| JUnit / Mockito | —    | Tests unitaires et intégration |

## 🚀 Démarrage rapide
### 🧩 Prérequis
- Java 17+
- Maven 3.9+
- PostgreSQL
- IDE : IntelliJ / Eclipse

### 🔧 Configuration
Crée un fichier `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agile_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
server.port=2025
```

### ▶️ Lancement
```bash
mvn spring-boot:run
```

API accessible sur `http://localhost:2025/api`

## 📁 Structure du projet
```
agilesprintplus-backend/
 ├── src/main/java/com/agilesprintplus/
 │   ├── api/          → Contrôleurs REST
 │   ├── model/        → Entités JPA
 │   ├── dto/          → DTOs
 │   ├── mapper/       → MapStruct
 │   ├── repository/   → DAO
 │   ├── service/      → Logique métier
 │   └── security/     → JWT et configuration
 └── pom.xml
```

## 👨‍💻 Auteur
**Pierre Landry Tchiengue**  
📧 ltchiengue73@gmail.com  
💼 [LinkedIn](https://www.linkedin.com/in/landry-pierre-tchiengue)
