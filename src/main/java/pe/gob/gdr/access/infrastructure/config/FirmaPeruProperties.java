package pe.gob.gdr.access.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.documentos.firma-peru")
public class FirmaPeruProperties {

    private String proveedorPrincipal = "FIRMA_PERU";
    private String urlInicioBase;
    private boolean integracionOficialHabilitada = false;
    private boolean consultaEstadoHabilitada = false;
    private boolean retornoAutomaticoHabilitado = false;
    private String mensajeIntegracion = "El sistema queda preparado para Firma Peru, pero este entorno aun no tiene convenio, credenciales ni callback oficial habilitado.";

    public String getProveedorPrincipal() {
        return proveedorPrincipal;
    }

    public void setProveedorPrincipal(String proveedorPrincipal) {
        this.proveedorPrincipal = proveedorPrincipal;
    }

    public String getUrlInicioBase() {
        return urlInicioBase;
    }

    public void setUrlInicioBase(String urlInicioBase) {
        this.urlInicioBase = urlInicioBase;
    }

    public boolean isIntegracionOficialHabilitada() {
        return integracionOficialHabilitada;
    }

    public void setIntegracionOficialHabilitada(boolean integracionOficialHabilitada) {
        this.integracionOficialHabilitada = integracionOficialHabilitada;
    }

    public boolean isConsultaEstadoHabilitada() {
        return consultaEstadoHabilitada;
    }

    public void setConsultaEstadoHabilitada(boolean consultaEstadoHabilitada) {
        this.consultaEstadoHabilitada = consultaEstadoHabilitada;
    }

    public boolean isRetornoAutomaticoHabilitado() {
        return retornoAutomaticoHabilitado;
    }

    public void setRetornoAutomaticoHabilitado(boolean retornoAutomaticoHabilitado) {
        this.retornoAutomaticoHabilitado = retornoAutomaticoHabilitado;
    }

    public String getMensajeIntegracion() {
        return mensajeIntegracion;
    }

    public void setMensajeIntegracion(String mensajeIntegracion) {
        this.mensajeIntegracion = mensajeIntegracion;
    }
}
