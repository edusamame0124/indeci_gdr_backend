package pe.gob.gdr.access.application.service;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.gdr.access.application.dto.request.IndicatorUpsertRequest;
import pe.gob.gdr.access.application.dto.response.IndicatorResponse;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.domain.exception.ResourceNotFoundException;
import pe.gob.gdr.access.domain.model.GdrFormula;
import pe.gob.gdr.access.domain.model.GdrIndicator;
import pe.gob.gdr.access.domain.model.GdrSegment;
import pe.gob.gdr.access.domain.model.GdrValueType;
import pe.gob.gdr.access.domain.repository.GdrFormulaRepository;
import pe.gob.gdr.access.domain.repository.GdrIndicatorRepository;
import pe.gob.gdr.access.domain.repository.GdrSegmentRepository;
import pe.gob.gdr.access.domain.repository.GdrValueTypeRepository;

@Service
public class GdrIndicatorService {

    private final GdrIndicatorRepository indicatorRepository;
    private final GdrValueTypeRepository valueTypeRepository;
    private final GdrFormulaRepository formulaRepository;
    private final GdrSegmentRepository segmentRepository;

    public GdrIndicatorService(
            GdrIndicatorRepository indicatorRepository,
            GdrValueTypeRepository valueTypeRepository,
            GdrFormulaRepository formulaRepository,
            GdrSegmentRepository segmentRepository
    ) {
        this.indicatorRepository = indicatorRepository;
        this.valueTypeRepository = valueTypeRepository;
        this.formulaRepository = formulaRepository;
        this.segmentRepository = segmentRepository;
    }

    public List<IndicatorResponse> listIndicators() {
        return indicatorRepository.findActive().stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional
    public IndicatorResponse createIndicator(IndicatorUpsertRequest request) {
        GdrIndicator indicator = new GdrIndicator();
        applyRequest(indicator, request);
        indicator.setCode(buildPendingCode());
        GdrIndicator persisted = indicatorRepository.save(indicator);

        String generatedCode = formatIndicatorCode(persisted.getId());
        if (indicatorRepository.existsByCodeIgnoreCaseAndIdNot(generatedCode, persisted.getId())) {
            throw new DomainException("Ya existe un indicador con el codigo generado automaticamente.");
        }
        persisted.setCode(generatedCode);
        return mapResponse(indicatorRepository.save(persisted));
    }

    @Transactional
    public IndicatorResponse updateIndicator(Long indicatorId, IndicatorUpsertRequest request) {
        GdrIndicator indicator = indicatorRepository.findActiveById(indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el indicador solicitado."));

        applyRequest(indicator, request);
        return mapResponse(indicatorRepository.save(indicator));
    }

    private void applyRequest(GdrIndicator indicator, IndicatorUpsertRequest request) {
        GdrValueType valueType = valueTypeRepository.findActiveById(request.valueTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el tipo de valor indicado."));
        GdrFormula formula = formulaRepository.findActiveById(request.formulaId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la formula indicada."));
        GdrSegment segment = segmentRepository.findActiveById(request.segmentId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el segmento indicado."));

        indicator.setName(request.name().trim());
        indicator.setDescription(normalizeOptionalText(request.description()));
        indicator.setValueType(valueType);
        indicator.setFormula(formula);
        indicator.setSegment(segment);
        indicator.setStatus("ACTIVE");
    }

    private IndicatorResponse mapResponse(GdrIndicator indicator) {
        return new IndicatorResponse(
                indicator.getId(),
                indicator.getCode(),
                indicator.getName(),
                indicator.getDescription(),
                indicator.getValueType().getId(),
                indicator.getValueType().getName(),
                indicator.getFormula().getId(),
                indicator.getFormula().getName(),
                indicator.getSegment().getId(),
                indicator.getSegment().getName(),
                indicator.getStatus()
        );
    }

    private String buildPendingCode() {
        String compactToken = Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase(Locale.ROOT);
        if (compactToken.length() > 12) {
            compactToken = compactToken.substring(compactToken.length() - 12);
        }
        return "TMP" + compactToken;
    }

    private String formatIndicatorCode(Long id) {
        return String.format("IND-%03d", id);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
