package pe.gob.gdr.access.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.domain.model.ActiveCycle;
import pe.gob.gdr.access.domain.model.DocSignedFile;
import pe.gob.gdr.access.domain.model.GdrImprovementOpportunity;
import pe.gob.gdr.access.domain.model.GdrResult;
import pe.gob.gdr.access.domain.model.GdrSolicitudConfirmacion;
import pe.gob.gdr.access.domain.policy.QualitativeRating;
import pe.gob.gdr.access.domain.repository.DocSignedFileRepository;
import pe.gob.gdr.access.domain.repository.GdrImprovementOpportunityRepository;
import pe.gob.gdr.access.domain.repository.GdrResultRepository;
import pe.gob.gdr.access.domain.repository.GdrSolicitudConfirmacionRepository;

/**
 * Agrega indicadores del ciclo activo para el informe de cierre (SRP).
 * CONSTRAINT: no modifica lógica de cálculo de evaluaciones.
 */
@Service
public class GdrInformeCierreConsolidador {

    private final GdrResultRepository resultRepository;
    private final GdrSolicitudConfirmacionRepository solicitudRepository;
    private final GdrImprovementOpportunityRepository improvementRepository;
    private final DocSignedFileRepository signedFileRepository;

    public GdrInformeCierreConsolidador(
            GdrResultRepository resultRepository,
            GdrSolicitudConfirmacionRepository solicitudRepository,
            GdrImprovementOpportunityRepository improvementRepository,
            DocSignedFileRepository signedFileRepository
    ) {
        this.resultRepository = resultRepository;
        this.solicitudRepository = solicitudRepository;
        this.improvementRepository = improvementRepository;
        this.signedFileRepository = signedFileRepository;
    }

    public InformeCierreSnapshot consolidar(ActiveCycle cycle) {
        List<GdrResult> results = resultRepository.findAllInActiveCycle();
        List<GdrSolicitudConfirmacion> solicitudes =
                solicitudRepository.findByCycleIdOrderByFechaSolicitud(cycle.getId());
        int oportunidades = improvementRepository.findAllInActiveCycle().size();
        int documentos = signedFileRepository.findAllInActiveCycle().size();

        int buen = 0;
        int observacion = 0;
        int desaprobado = 0;
        int distinguido = 0;
        for (GdrResult result : results) {
            String code = result.getQualitativeRatingCode();
            if (QualitativeRating.BUEN_RENDIMIENTO.code().equals(code)) {
                buen++;
            } else if (QualitativeRating.SUJETO_OBSERVACION.code().equals(code)) {
                observacion++;
            } else if (QualitativeRating.DESAPROBADO.code().equals(code)) {
                desaprobado++;
            } else if (QualitativeRating.DISTINGUIDO.code().equals(code)) {
                distinguido++;
            }
        }

        int confirmacionesResueltas = (int) solicitudes.stream()
                .filter(s -> GdrSolicitudConfirmacion.ESTADO_RESUELTA.equals(s.getEstado()))
                .count();

        return new InformeCierreSnapshot(
                results.size(),
                buen,
                observacion,
                desaprobado,
                distinguido,
                oportunidades,
                solicitudes.size(),
                confirmacionesResueltas,
                documentos
        );
    }

    public record InformeCierreSnapshot(
            int totalEvaluados,
            int totalBuenRendimiento,
            int totalSujetoObservacion,
            int totalDesaprobado,
            int totalDistinguido,
            int totalOportunidadesMejora,
            int totalConfirmaciones,
            int totalConfirmacionesResueltas,
            int totalDocumentosFirmados
    ) {
    }
}
