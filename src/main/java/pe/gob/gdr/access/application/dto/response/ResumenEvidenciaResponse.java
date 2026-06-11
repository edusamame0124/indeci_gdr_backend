package pe.gob.gdr.access.application.dto.response;

import java.time.LocalDate;

public record ResumenEvidenciaResponse(
        Long id,
        Long goalId,
        String title,
        String detail,
        String evidenceTypeCode,
        String evidenceTypeName,
        String expectedFormatCode,
        String expectedFormatName,
        LocalDate expectedDate,
        String fileOriginalName,
        Long fileSizeBytes,
        boolean fileAvailable,
        String statusCode,
        String statusName,
        String latestReviewComment,
        String latestReviewQualificationCode,
        String latestReviewQualificationName,
        boolean mandatory
) {
}
