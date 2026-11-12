# RailBook – Railway Reservation System: Cloud & Vercel Deployment Guide

## 1. Executive Summary & Root Cause Analysis

### The Initial Deployment Issue: "This page doesn’t exist" (404 NOT_FOUND)
When the repository was first imported into Vercel, Vercel reported **"Deployed to Production"**, yet visiting the URL returned a 404 error: *"This page doesn't exist"*.

#### Technical Root Cause:
1. **Frontend Architecture**: RailBook is an SSR (Server-Side Rendered) Java application built with **Spring Boot 4.1.2**, **Java 21**, **Thymeleaf**, and **Spring Security**. Its HTML views reside in `src/main/resources/templates/` and require the JVM and embedded Apache Tomcat servlet container to compile, evaluate expressions (`th:text`, `th:each`, CSRF tokens), manage HTTP sessions (`JSESSIONID`), and serve responses dynamically.
2. **Missing Build Spec**: Without a `Dockerfile`, `Dockerfile.vercel`, or `vercel.json`, Vercel's zero-configuration heuristic treated the repository as a static JavaScript/HTML site.
3. **Empty Static Deployment**: Since no static root `index.html` or build output directory existed, Vercel deployed an empty static site. The edge router consequently served the default 404 handler for all incoming routes.

---

## 2. Platform Architecture & Compatibility Analysis

### Can Vercel Host Spring Boot?

| Architecture Aspect | Vercel Standard / Serverless | Vercel Container Runtime (Beta) | Dedicated Java Backend (Railway / Render / AWS) |
| :--- | :--- | :--- | :--- |
| **Java 21 / JVM Support** | ❌ No native Java runtime | ✅ Supported via OCI `Dockerfile.vercel` | ✅ Fully native continuous Java 21 support |
| **Scale-to-Zero Behavior** | Automatically scales to 0 | Scales to 0 after 30s (preview) or 5m (prod) | Always-on (no cold start timeouts) |
| **Cold Start Impact** | N/A | **10–25+ seconds** JVM & Hibernate startup | **0 seconds** (continuous server process) |
| **Session Management (`JSESSIONID`)**| ❌ Ephemeral | ⚠️ In-memory sessions drop on instance rotation | ✅ Consistent persistent HTTP session support |
| **Concurrency & Pessimistic Locks** | ❌ Risky across spun-up containers | ⚠️ Connection pool saturation on DB | ✅ Managed HikariCP pool with row-level locks |
| **Relational Database** | ❌ No MariaDB/MySQL provided | ❌ Requires external Cloud DB | ❌ Requires Cloud DB or provisioned addon |

---

## 3. Recommended Production Architectures

### Architecture A: Vercel Edge Reverse Proxy + Dedicated Java Backend (Best Practice)
In this industry-standard model:
1. **Vercel** serves as the public entrypoint (`https://railbook.vercel.app`), providing global edge SSL termination, DDoS protection, edge caching for `/css/**`, `/js/**`, and `/images/**`, and routing.
2. A **Dedicated Java Platform** (such as Railway, Render, Fly.io, or AWS ECS) runs the continuous Spring Boot 4.1.2 application on Java 21.
3. A **Managed MariaDB / MySQL** instance (Aiven, Railway MariaDB, PlanetScale, Supabase) stores database records securely.

#### Vercel Configuration for Reverse Proxy (`vercel.json`):
```json
{
  "version": 2,
  "rewrites": [
    {
      "source": "/(.*)",
      "destination": "https://YOUR-BACKEND-DOMAIN.up.railway.app/$1"
    }
  ]
}
```

---

### Architecture B: Vercel Container Runtime (Direct Docker Deployment)
For single-platform deployment directly on Vercel:
1. Vercel builds the multi-stage `Dockerfile.vercel`.
2. The container listens dynamically on `$PORT`.
3. An external cloud MariaDB database is connected via Vercel Project Environment Variables.

#### Files Configured:
- `Dockerfile.vercel`: Multi-stage Eclipse Temurin JDK 21 build stage + minimal JRE 21 runtime stage.
- `vercel.json`:
  ```json
  {
    "$schema": "https://openapi.vercel.sh/vercel.json",
    "framework": "container"
  }
  ```

---

## 4. Required Environment Variables

Configure these in your production hosting platform dashboard (Vercel Project Settings > Environment Variables, or Railway/Render Environment Variables). **Never commit these values to Git:**

| Variable Name | Description | Example Production Value |
| :--- | :--- | :--- |
| `PORT` | HTTP server port | `8080` (auto-assigned by platform) |
| `DB_URL` | JDBC URL to cloud MariaDB/MySQL | `jdbc:mariadb://db.your-provider.com:3306/railbook_db?useSSL=true` |
| `DB_USERNAME` | Database username | `railbook_user` |
| `DB_PASSWORD` | Database password | `(secret cloud password)` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate schema management | `update` |
| `DEMO_DATA_ENABLED` | Seed demo stations and trains | `true` (or `false` once seeded) |

---

## 5. Health Check Endpoint
RailBook includes a production health probe accessible without authentication:
- **Endpoint**: `GET /health`
- **Response**:
  ```json
  {
    "status": "UP",
    "service": "RailBook - Railway Reservation System",
    "timestamp": "2026-11-12T11:05:00.000Z"
  }
  ```

---

## 6. Database Migration & Cloud Seed

The database schema and sample data can be initialized automatically by Spring Boot (`spring.jpa.hibernate.ddl-auto=update` and `DataInitializer.java`), or seeded directly into your cloud MariaDB instance using the standard SQL dump:
```bash
mariadb -h <CLOUD_HOST> -P <PORT> -u <USER> -p <DB_NAME> < railbook_db_backup.sql
```
