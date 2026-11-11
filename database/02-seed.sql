/* =====================================================================
   Ufinet Autos - Datos precargados (opcional)
   Usuarios de prueba (contraseña de ambos: Password123):
     - demo@ufinet.com
     - maria@ufinet.com
   Idempotente: no duplica registros si se ejecuta de nuevo.
   ===================================================================== */
USE ufinet_autos;
GO

SET NOCOUNT ON;

/* Hash BCrypt (cost 10) de "Password123", generado con BCryptPasswordEncoder de Spring Security */
DECLARE @password_hash NVARCHAR(100) = N'$2a$10$Ju9ZUrcIqgOcGDxP6iPrO.qcdjjoRPmqcEoITyPvD8GuF0hsjLonq';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'demo@ufinet.com')
    INSERT INTO dbo.users (full_name, email, password_hash) VALUES (N'Usuario Demo', N'demo@ufinet.com', @password_hash);

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = N'maria@ufinet.com')
    INSERT INTO dbo.users (full_name, email, password_hash) VALUES (N'María Rodríguez', N'maria@ufinet.com', @password_hash);

DECLARE @demo_id  BIGINT = (SELECT id FROM dbo.users WHERE email = N'demo@ufinet.com');
DECLARE @maria_id BIGINT = (SELECT id FROM dbo.users WHERE email = N'maria@ufinet.com');

MERGE dbo.cars AS target
USING (VALUES
    (@demo_id,  N'Toyota',     N'Corolla',  2022, N'ABC-123', N'Blanco', NULL),
    (@demo_id,  N'Toyota',     N'Hilux',    2020, N'HLX-200', N'Gris',   NULL),
    (@demo_id,  N'Mazda',      N'CX-5',     2023, N'MZD-555', N'Rojo',   NULL),
    (@demo_id,  N'Chevrolet',  N'Spark GT', 2018, N'CHV-018', N'Azul',   NULL),
    (@demo_id,  N'Kia',        N'Rio',      2021, N'KIA-321', N'Negro',  NULL),
    (@maria_id, N'Nissan',     N'Versa',    2019, N'NSN-190', N'Plata',  NULL),
    (@maria_id, N'Hyundai',    N'Tucson',   2024, N'HYD-240', N'Verde',  NULL)
) AS source (user_id, brand, model, model_year, plate_number, color, photo_url)
ON target.plate_number = source.plate_number
WHEN NOT MATCHED THEN
    INSERT (user_id, brand, model, model_year, plate_number, color, photo_url)
    VALUES (source.user_id, source.brand, source.model, source.model_year, source.plate_number, source.color, source.photo_url);

PRINT 'Datos de prueba cargados.';
GO
