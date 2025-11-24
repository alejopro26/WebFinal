# ChatNexus – Demo Frontend

## Arranque rápido

1. Instalar dependencias
   ```bash
   npm install
   ```

2. Levantar el backend (desde la raíz del proyecto)
   ```bash
   cd ../..
   mvn spring-boot:run -Dspring.profiles.active=dev
   ```

3. Levantar el frontend (en esta carpeta)
   ```bash
   npm run dev
   ```

4. Abrir http://localhost:5173 y loguearte con cualquier usuario registrado.

## Scripts útiles

- `npm run dev` – servidor de desarrollo con hot-reload y proxy a `localhost:8080/api`
- `npm run build` – build de producción en `dist/`
- `npm run lint` – ESLint sin warnings
- `npm run preview` – previsualizar build local

## Características visibles para el profesor

- **Notificaciones flotantes** (sin `alert()`)
- **Panel de Salud y Métricas HTTP** con conteo por método/estado y agregados `2xx/3xx/4xx/5xx`
- **Moderación en tiempo real**: ban/unban servidor, mute/unmute canal
- **Permisos por rol**: OWNER / MODERATOR / MEMBER con ACL por canal
- **Audit trail**: botones “Ver auditoría ACL / MOD” con últimos 10 eventos
- **Chat multicanal**: salas públicas/privadas, mensajes directos, archivos adjuntos
- **Presencia y “escribiendo…”** vía WebSocket

## Tecnologías

React 19 + Vite + Tailwind CSS + SockJS/STOMP + Mantine (componentes) + Framer Motion (animaciones).

## Captura de UI

![ui-preview](https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=Clean%20dark%20chat%20UI%20with%20sidebar%20rooms%2C%20floating%20toasts%2C%20health%20metrics%20panel%2C%20Spanish%20labels%2C%20modern%20Discord-like%20colors%2C%20desktop%20viewport&image_size=landscape_16_9)