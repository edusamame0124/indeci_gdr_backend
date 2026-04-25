package pe.gob.gdr.access.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.gob.gdr.access.application.dto.response.CatalogItemResponse;
import pe.gob.gdr.access.domain.repository.GdrFormulaRepository;
import pe.gob.gdr.access.domain.repository.GdrSegmentRepository;
import pe.gob.gdr.access.domain.repository.GdrValueTypeRepository;

@Service
public class GdrCatalogService {

    private final GdrValueTypeRepository valueTypeRepository;
    private final GdrFormulaRepository formulaRepository;
    private final GdrSegmentRepository segmentRepository;

    public GdrCatalogService(
            GdrValueTypeRepository valueTypeRepository,
            GdrFormulaRepository formulaRepository,
            GdrSegmentRepository segmentRepository
    ) {
        this.valueTypeRepository = valueTypeRepository;
        this.formulaRepository = formulaRepository;
        this.segmentRepository = segmentRepository;
    }

    public List<CatalogItemResponse> listValueTypes() {
        return valueTypeRepository.findActive().stream()
                .map(item -> new CatalogItemResponse(item.getId(), item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    public List<CatalogItemResponse> listFormulas() {
        return formulaRepository.findActive().stream()
                .map(item -> new CatalogItemResponse(item.getId(), item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }

    public List<CatalogItemResponse> listSegments() {
        return segmentRepository.findActive().stream()
                .map(item -> new CatalogItemResponse(item.getId(), item.getCode(), item.getName(), item.getDescription()))
                .toList();
    }
}
