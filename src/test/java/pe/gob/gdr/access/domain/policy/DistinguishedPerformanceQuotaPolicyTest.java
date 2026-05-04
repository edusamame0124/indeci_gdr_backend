package pe.gob.gdr.access.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DistinguishedPerformanceQuotaPolicyTest {

    @Test
    void maxSlots_respectsTenPercentCeiling() {
        assertEquals(0, DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(0));
        assertEquals(1, DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(1));
        assertEquals(1, DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(9));
        assertEquals(1, DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(10));
        assertEquals(2, DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(11));
        assertEquals(3, DistinguishedPerformanceQuotaPolicy.maxDistinguishingSlots(25));
    }
}
