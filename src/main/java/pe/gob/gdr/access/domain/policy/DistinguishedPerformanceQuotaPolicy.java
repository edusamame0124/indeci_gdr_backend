package pe.gob.gdr.access.domain.policy;

public final class DistinguishedPerformanceQuotaPolicy {

    /** Share of distinguished slots relative to servants with qualification notified per cycle (normative guideline). */
    public static final double DISTINGUIDO_SHARE = 0.10d;

    private DistinguishedPerformanceQuotaPolicy() {
    }

    /** Maximum Rendimiento distinguido concurrent assignments permitted for an evaluation cycle technical ceiling. */
    public static int maxDistinguishingSlots(int notifiedUniverseTotal) {
        if (notifiedUniverseTotal <= 0) {
            return 0;
        }
        int ceiling = (int) Math.ceil(notifiedUniverseTotal * DISTINGUIDO_SHARE);
        return Math.max(ceiling, 0);
    }
}
