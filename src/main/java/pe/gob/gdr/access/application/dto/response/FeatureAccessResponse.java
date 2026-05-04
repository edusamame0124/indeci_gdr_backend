package pe.gob.gdr.access.application.dto.response;

public record FeatureAccessResponse(
        boolean canAccessDashboard,
        boolean canViewAssignments,
        boolean canViewCatalogs,
        boolean canViewIndicators,
        boolean canManageIndicators,
        boolean canViewGoals,
        boolean canManageGoals,
        boolean canViewEvidences,
        boolean canManageEvidences,
        boolean canReviewEvidences,
        boolean canViewFinalEvaluations,
        boolean canManageFinalEvaluations,
        boolean canViewResults,
        boolean canViewDocuments,
        boolean canPrepareDocuments,
        boolean canStartSignatureFlow,
        boolean canRegisterSignatureReturn,
        boolean canRegisterSignedDocuments,
        boolean canViewImprovements,
        boolean canManageImprovements,
        boolean canFollowupImprovements,
        boolean canViewReports,
        boolean canViewNotifications,
        boolean canViewConsents,
        boolean canViewOrhReception,
        boolean canManageUsers,
        boolean canViewDistinguidoCandidates,
        boolean canManageDistinguidoRequisites,
        boolean canAssignDistinguido
) {
}
