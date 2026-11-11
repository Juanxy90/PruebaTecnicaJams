/* =====================================================================
   Ufinet Autos - Script de creación de base de datos y tablas
   Motor: SQL Server 2019+
   Idempotente: se puede ejecutar varias veces sin error.
   ===================================================================== */

IF DB_ID(N'ufinet_autos') IS NULL
BEGIN
    CREATE DATABASE ufinet_autos;
END
GO

USE ufinet_autos;
GO

/* ---------------------------------------------------------------------
   Tabla: users  (propiedad del microservicio auth-service)
   --------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id             BIGINT IDENTITY(1,1) NOT NULL,
        full_name      NVARCHAR(100)        NOT NULL,
        email          NVARCHAR(150)        NOT NULL,
        password_hash  NVARCHAR(100)        NOT NULL,
        created_at     DATETIME2(6)         NOT NULL CONSTRAINT df_users_created_at DEFAULT SYSDATETIME(),
        CONSTRAINT pk_users PRIMARY KEY (id),
        CONSTRAINT uq_users_email UNIQUE (email)
    );
END
GO

/* ---------------------------------------------------------------------
   Tabla: cars  (propiedad del microservicio car-service)
   user_id es llave foránea a users(id). Al eliminar un usuario se eliminan sus autos.
   --------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.cars', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.cars (
        id            BIGINT IDENTITY(1,1) NOT NULL,
        user_id       BIGINT               NOT NULL,
        brand         NVARCHAR(50)         NOT NULL,
        model         NVARCHAR(50)         NOT NULL,
        model_year    INT                  NOT NULL,
        plate_number  NVARCHAR(10)         NOT NULL,
        color         NVARCHAR(30)         NOT NULL,
        photo_url     NVARCHAR(500)        NULL,
        created_at    DATETIME2(6)         NOT NULL CONSTRAINT df_cars_created_at DEFAULT SYSDATETIME(),
        updated_at    DATETIME2(6)         NOT NULL CONSTRAINT df_cars_updated_at DEFAULT SYSDATETIME(),
        CONSTRAINT pk_cars PRIMARY KEY (id),
        CONSTRAINT uq_cars_plate_number UNIQUE (plate_number),
        CONSTRAINT fk_cars_users FOREIGN KEY (user_id) REFERENCES dbo.users (id) ON DELETE CASCADE,
        CONSTRAINT ck_cars_model_year CHECK (model_year >= 1886)
    );
END
GO

/* Índices para las búsquedas y filtros más comunes (siempre por usuario) */
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_cars_user_brand' AND object_id = OBJECT_ID(N'dbo.cars'))
    CREATE INDEX ix_cars_user_brand ON dbo.cars (user_id, brand);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_cars_user_year' AND object_id = OBJECT_ID(N'dbo.cars'))
    CREATE INDEX ix_cars_user_year ON dbo.cars (user_id, model_year);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_cars_user_model' AND object_id = OBJECT_ID(N'dbo.cars'))
    CREATE INDEX ix_cars_user_model ON dbo.cars (user_id, model);
GO

PRINT 'Esquema ufinet_autos listo.';
GO
