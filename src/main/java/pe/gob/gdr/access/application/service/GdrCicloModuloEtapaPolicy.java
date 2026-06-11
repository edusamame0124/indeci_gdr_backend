package pe.gob.gdr.access.application.service;

import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * G-01 — Política de módulo+etapa del ciclo GDR.
 * Determina si un módulo (slug) es operacional en una etapa dada.
 * Referencia: RPE 068-2020-SERVIR-PE Art. 5, registry del tablero frontend.
 *
 * <p>Resultado de la política:
 * <ul>
 *   <li>OPERATIONAL   — el módulo está activo y editable</li>
 *   <li>READ_ONLY     — el módulo existe pero no es editable</li>
 *   <li>NOT_APPLICABLE — el módulo no aplica en esta etapa</li>
 * </ul>
 */
@Component("gdrCicloModuloEtapaPolicy")
public class GdrCicloModuloEtapaPolicy {

    public enum AccesoModulo { OPERATIONAL, READ_ONLY, NOT_APPLICABLE }

    // Etapas en las que cada módulo-slug es OPERATIONAL (editable)
    private static final Map<String, Set<String>> OPERATIONAL_STAGES = Map.ofEntries(
            // Bloque A — Planificación
            Map.entry("cronograma",       Set.of("BORRADOR", "EN_PLANIFICACION")),
            Map.entry("participacion",    Set.of("EN_PLANIFICACION")),
            Map.entry("asignaciones",     Set.of("EN_PLANIFICACION")),
            // Bloque B — Indicadores y metas
            Map.entry("indicadores",      Set.of("EN_PLANIFICACION", "EN_SEGUIMIENTO")),
            Map.entry("metas",            Set.of("EN_PLANIFICACION", "EN_SEGUIMIENTO")),
            Map.entry("seguimiento",      Set.of("EN_SEGUIMIENTO")),
            // Bloque C — Evaluación
            Map.entry("evaluacion-final", Set.of("EN_EVALUACION")),
            Map.entry("retroalimentacion",Set.of("EN_EVALUACION")),
            Map.entry("confirmacion",     Set.of("EN_CONFIRMACION")),
            // Bloque D — Distinguido
            Map.entry("cie",              Set.of("EN_CONFIRMACION", "EN_RENDIMIENTO_DISTINGUIDO")),
            Map.entry("distinguido",      Set.of("EN_RENDIMIENTO_DISTINGUIDO")),
            // Bloque E — Cierre
            Map.entry("informe-cierre",   Set.of("EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            // Bloque F — Transversal (siempre OPERATIONAL si ciclo activo)
            Map.entry("evidencias",       Set.of("EN_PLANIFICACION", "EN_SEGUIMIENTO", "EN_EVALUACION")),
            Map.entry("oportunidades",    Set.of("EN_PLANIFICACION", "EN_SEGUIMIENTO", "EN_EVALUACION")),
            Map.entry("documentos",       Set.of("EN_EVALUACION", "EN_CONFIRMACION", "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("auditoria",        Set.of("EN_SEGUIMIENTO", "EN_EVALUACION", "EN_CONFIRMACION",
                                                 "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO"))
    );

    // Etapas en las que un módulo es READ_ONLY (visible pero no editable)
    private static final Map<String, Set<String>> READ_ONLY_STAGES = Map.ofEntries(
            Map.entry("cronograma",       Set.of("EN_SEGUIMIENTO", "EN_EVALUACION", "EN_CONFIRMACION",
                                                 "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("participacion",    Set.of("EN_SEGUIMIENTO", "EN_EVALUACION", "EN_CONFIRMACION",
                                                 "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("asignaciones",     Set.of("EN_SEGUIMIENTO", "EN_EVALUACION", "EN_CONFIRMACION",
                                                 "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("metas",            Set.of("EN_EVALUACION", "EN_CONFIRMACION",
                                                 "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("evaluacion-final", Set.of("EN_CONFIRMACION", "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("retroalimentacion",Set.of("EN_CONFIRMACION", "EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("confirmacion",     Set.of("EN_RENDIMIENTO_DISTINGUIDO", "CERRADO")),
            Map.entry("distinguido",      Set.of("CERRADO")),
            Map.entry("informe-cierre",   Set.of("CERRADO")),
            Map.entry("documentos",       Set.of("EN_PLANIFICACION", "EN_SEGUIMIENTO"))
    );

    /**
     * Evalúa si un módulo-slug es accesible (y con qué nivel) en la etapa dada.
     */
    public AccesoModulo evaluate(String moduleSlug, String estadoEtapa) {
        if (moduleSlug == null || estadoEtapa == null) {
            return AccesoModulo.NOT_APPLICABLE;
        }
        Set<String> operational = OPERATIONAL_STAGES.get(moduleSlug);
        if (operational != null && operational.contains(estadoEtapa)) {
            return AccesoModulo.OPERATIONAL;
        }
        Set<String> readOnly = READ_ONLY_STAGES.get(moduleSlug);
        if (readOnly != null && readOnly.contains(estadoEtapa)) {
            return AccesoModulo.READ_ONLY;
        }
        return AccesoModulo.NOT_APPLICABLE;
    }

    /** Retorna true si el módulo es editable en la etapa dada. */
    public boolean isOperational(String moduleSlug, String estadoEtapa) {
        return AccesoModulo.OPERATIONAL == evaluate(moduleSlug, estadoEtapa);
    }

    /** Retorna true si el módulo es visible (aunque sea solo lectura) en la etapa dada. */
    public boolean isAccessible(String moduleSlug, String estadoEtapa) {
        AccesoModulo result = evaluate(moduleSlug, estadoEtapa);
        return result != AccesoModulo.NOT_APPLICABLE;
    }
}
