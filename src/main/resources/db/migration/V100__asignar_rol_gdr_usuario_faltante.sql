WHENEVER SQLERROR EXIT FAILURE ROLLBACK
ALTER SESSION SET CURRENT_SCHEMA = &&APP_DB_SCHEMA;

-- Backfill: usuarios activos vinculados a una HR_PERSON activa que fueron dados de
-- alta en "Gestion de usuarios" sin marcar el rol GDR_USUARIO. Sin ese rol, la
-- persona queda invisible para la busqueda de candidatos en Participacion GDR
-- (JpaHrPersonRepository.findEligibleForAssignment exige EXISTS SEC_USER_ROLE
-- activa con ROLE_CODE = 'GDR_USUARIO').
--
-- Solo INSERTA filas que no existen (ningun registro SEC_USER_ROLE para ese
-- usuario+rol, sin importar su estado). No reactiva filas GDR_USUARIO que ya
-- existan en estado INACTIVE: esas pueden representar una revocacion deliberada
-- de un administrador y deben revisarse manualmente, no revertirse en bloque.
MERGE INTO SEC_USER_ROLE target
USING (
    SELECT
        user_account.ID_USER AS ID_USER,
        role_definition.ID_ROLE AS ID_ROLE
    FROM SEC_USER user_account
    JOIN HR_PERSON person
      ON person.ID_PERSON = user_account.ID_PERSON
     AND UPPER(person.STATUS) = 'ACTIVE'
    JOIN SEC_ROLE role_definition
      ON role_definition.ROLE_CODE = 'GDR_USUARIO'
     AND UPPER(role_definition.STATUS) = 'ACTIVE'
    WHERE UPPER(user_account.STATUS) = 'ACTIVE'
      AND NOT EXISTS (
          SELECT 1
          FROM SEC_USER_ROLE existing
          WHERE existing.ID_USER = user_account.ID_USER
            AND existing.ID_ROLE = role_definition.ID_ROLE
      )
) source
ON (target.ID_USER = source.ID_USER AND target.ID_ROLE = source.ID_ROLE)
WHEN NOT MATCHED THEN
    INSERT (ID_USER_ROLE, ID_USER, ID_ROLE, STATUS, ASSIGNED_AT)
    VALUES (SQ_SEC_USER_ROLE.NEXTVAL, source.ID_USER, source.ID_ROLE, 'ACTIVE', SYSTIMESTAMP);
