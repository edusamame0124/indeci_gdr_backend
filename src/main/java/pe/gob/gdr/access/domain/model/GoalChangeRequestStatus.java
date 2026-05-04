package pe.gob.gdr.access.domain.model;

public enum GoalChangeRequestStatus {
    PENDIENTE("Pendiente"),
    REVISADO("Revisado por ORH");

    private final String displayName;

    GoalChangeRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
