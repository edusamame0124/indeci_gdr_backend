package pe.gob.gdr.access.domain.model;

public enum GoalOrhSubmissionStatus {
    ENVIADO("Enviado a ORH"),
    REVISADO("Revisado por ORH");

    private final String displayName;

    GoalOrhSubmissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
