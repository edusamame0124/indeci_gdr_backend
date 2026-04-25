# Carga manual de logos BLOB

Este proyecto no incluye módulo administrativo de branding.  
La carga real de `MAIN_LOGO` y `HEADER_LOGO` se hace de forma controlada por DBA.

## 1. Verificar branding activo

```sql
SELECT ID_BRANDING, INSTITUTION_NAME, STATUS
FROM SEC_INSTITUTION_BRANDING
WHERE STATUS = 'ACTIVE';
```

Debe existir como máximo una fila `ACTIVE`.

## 2. Preparar archivos reales

- `main_logo.png` o `main_logo.jpg`
- `header_logo.png` o `header_logo.jpg`

MIME permitidos por diseño:

- `image/png`
- `image/jpeg`
- `image/webp`
- `image/svg+xml`

## 3. Crear DIRECTORY en Oracle

Ejemplo:

```sql
CREATE OR REPLACE DIRECTORY GDR_BRANDING_DIR AS '/ruta/del/servidor/branding';
GRANT READ ON DIRECTORY GDR_BRANDING_DIR TO GDR_ACCESS;
```

Cambie `GDR_ACCESS` por el valor real de `APP_DB_SCHEMA`.

## 4. Ejecutar el script operativo

Desde SQL*Plus o SQLcl:

```sql
DEFINE APP_DB_SCHEMA = GDR_ACCESS
DEFINE ORACLE_DIR_NAME = GDR_BRANDING_DIR
DEFINE MAIN_LOGO_FILE = main_logo.png
DEFINE MAIN_LOGO_MIME = image/png
DEFINE HEADER_LOGO_FILE = header_logo.png
DEFINE HEADER_LOGO_MIME = image/png
@manual_load_branding_logos.sql
```

## 5. Validar carga

```sql
SELECT
    ID_BRANDING,
    INSTITUTION_NAME,
    DBMS_LOB.GETLENGTH(MAIN_LOGO) AS MAIN_LOGO_BYTES,
    MAIN_LOGO_MIME_TYPE,
    DBMS_LOB.GETLENGTH(HEADER_LOGO) AS HEADER_LOGO_BYTES,
    HEADER_LOGO_MIME_TYPE
FROM SEC_INSTITUTION_BRANDING
WHERE STATUS = 'ACTIVE';
```

## 6. Validar desde backend

Una vez levantado el backend:

- `GET /api/gdr-access/public/branding/login`
- `GET /api/gdr-access/public/branding/login/logo/header`
- `GET /api/gdr-access/public/branding/login/logo/main`

Si los BLOB están correctamente cargados, los endpoints de imagen responderán con `200 OK` y el `Content-Type` configurado.
