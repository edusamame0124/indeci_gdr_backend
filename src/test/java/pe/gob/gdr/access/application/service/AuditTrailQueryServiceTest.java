package pe.gob.gdr.access.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pe.gob.gdr.access.application.dto.response.AuditEventResponse;
import pe.gob.gdr.access.application.dto.response.PageResponse;
import pe.gob.gdr.access.domain.model.AuditEvent;
import pe.gob.gdr.access.domain.repository.AuditEventRepository;

@ExtendWith(MockitoExtension.class)
class AuditTrailQueryServiceTest {

    @Mock AuditEventRepository auditEventRepository;
    @Mock AuditTrailService auditTrailService;

    private AuditTrailQueryService sut;

    @BeforeEach
    void setUp() {
        sut = new AuditTrailQueryService(auditEventRepository, auditTrailService);
    }

    @Test
    void buscar_mapeaEventosPaginados() {
        AuditEvent event = AuditEvent.builder()
                .id(1L)
                .eventCode("LOGIN_SUCCESS")
                .principal("usuario.gdr")
                .detail("Autenticacion exitosa.")
                .occurredAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        Page<AuditEvent> page = new PageImpl<>(List.of(event), PageRequest.of(0, 25), 1);
        when(auditEventRepository.search(any(), any(), any(), any(), any())).thenReturn(page);

        PageResponse<AuditEventResponse> response = sut.buscar(null, null, null, null, PageRequest.of(0, 25));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().eventCode()).isEqualTo("LOGIN_SUCCESS");
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void exportCsv_registraAuditoriaYGeneraCabeceras() {
        when(auditEventRepository.search(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        byte[] csv = sut.exportCsv(null, null, null, null, PageRequest.of(0, 100), "auditor.gdr");

        assertThat(new String(csv)).startsWith("id,eventCode,principal");
        verify(auditTrailService).recordEvent(
                eq("AUDIT_TRAIL_EXPORTADO"),
                eq("auditor.gdr"),
                any(),
                eq(null)
        );
    }
}
