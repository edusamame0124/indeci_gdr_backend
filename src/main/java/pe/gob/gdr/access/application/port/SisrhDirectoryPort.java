package pe.gob.gdr.access.application.port;

import java.util.List;

/**
 * Puerto de salida hacia el directorio de usuarios GDR_USUARIO del SISRH.
 * Permite que GDR muestre en el buscador de "Asignar Rol GDR" a las personas
 * registradas en el SISRH aunque aun no hayan iniciado sesion en GDR.
 *
 * Implementaciones deben degradar con elegancia: ante error o integracion
 * deshabilitada devuelven lista vacia (la busqueda cae a solo candidatos
 * locales, nunca rompe el modal).
 */
public interface SisrhDirectoryPort {

    List<SisrhDirectoryUser> searchGdrUsers(String query);
}
