package pe.gob.gdr.access.application.port;

/**
 * Usuario GDR_USUARIO tal como lo expone el directorio del SISRH (sistema
 * 'rendimiento'). GDR usa el {@code dni} como llave puente hacia HR_PERSON y el
 * {@code areaCodigo} para resolver la unidad organica al aprovisionar.
 */
public record SisrhDirectoryUser(
        String dni,
        String nombreCompleto,
        String username,
        String areaCodigo,
        String areaNombre,
        String estado
) {
}
