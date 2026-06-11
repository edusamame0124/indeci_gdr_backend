ALTER TABLE GDR_EVIDENCIA ADD (
    IS_MANDATORY NUMBER(1,0) DEFAULT 0 NOT NULL
);

COMMENT ON COLUMN GDR_EVIDENCIA.IS_MANDATORY IS 'Indica si la evidencia esperada es obligatoria para la meta (1=Si, 0=No)';
