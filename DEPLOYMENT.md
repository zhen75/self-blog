# Production deployment checklist

1. Generate new database and administrator passwords. The old values that were once committed must never be reused.
2. Place `BLOG_DB_URL`, `BLOG_DB_USERNAME`, `BLOG_DB_PASSWORD`, `BLOG_ADMIN_USERNAME`, and `BLOG_ADMIN_PASSWORD` in the service manager's protected environment file. Do not create or commit a `.env` file in this repository.
3. Start with the production profile: `SPRING_PROFILES_ACTIVE=prod`. It binds Spring Boot to `127.0.0.1` and requires the reverse proxy to terminate HTTPS.
4. Put Nginx or Caddy in front of the application. It must be the only public listener and must overwrite, not trust, client-provided forwarding headers.
5. Bind MySQL only to localhost or a private network. Grant the blog account only the CRUD privileges needed for the `blog` database.
6. Configure a 512 KB request-body limit in the reverse proxy, automated database backups, and a restore test before public launch.
7. Use a persistent shared rate-limit store such as Redis before running more than one application instance. The built-in limiter is bounded and safe for a single instance, but its counters reset after restart.
