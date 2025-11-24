# ChatNexus – Demo Full-Stack

Chat en tiempo real con salas públicas/privadas, canales, mensajes directos, moderación y auditoría.

## Requisitos

- Java 17+
- Maven 3.8+
- Node 18+ (pnpm o npm)
- MySQL 8 (o base en memoria para demo rápida)

## Arranque ultra-rápido (demo local)

1. Clonar y entrar a la carpeta raíz del proyecto.

2. Levantar backend + frontend con un solo comando:
   ```bash
   npm run demo
   ```
   (o `pnpm demo` si tienes pnpm).

3. Abrir http://localhost:5173 → login con usuario existente o regístrate.

4. Explora:
   - Crear salas (públicas o con contraseña)
   - Unirte a canales dentro de servidores
   - Enviar mensajes y archivos
   - Panel “Salud y Métricas” (health + conteos HTTP)
   - Moderación: ban/unban servidor, mute/unmute canal
   - Auditoría ACL/MOD (últimos 10 eventos)

## Comandos por separado

Backend:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

Frontend:
```bash
cd frontend/chat-frontend
npm install
npm run dev
```

Tests:
```bash
mvn test                 # backend
npm run test -- --run    # frontend (Vitest)
```

Build producción:
```bash
mvn clean package -DskipTests   # backend JAR
npm run build                   # frontend dist/
```

## Seguridad y observabilidad (solo en dev)

- Endpoints `/api/actuator/health`, `/metrics`, `/info`, `/env` visibles tras login.
- Auditoría en memoria: `/api/audit?type=ACL|MOD`.
- JWT stateless; contraseñas con BCrypt.

## Tecnologías

Backend: Spring Boot 3, Spring Security, WebSocket/STOMP, JPA, MySQL.
Frontend: React 19, Vite, Tailwind CSS, Mantine, SockJS, Framer Motion.

## Captura de UI

![ui-preview](https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=Clean%20dark%20chat%20UI%20with%20sidebar%20rooms%2C%20floating%20toasts%2C%20health%20metrics%20panel%2C%20Spanish%20labels%2C%20modern%20Discord-like%20colors%2C%20desktop%20viewport&image_size=landscape_16_9)