package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleMatchResponseDTO {
    private String candidateId;
    private String candidateName;
    private String jobId;
    private Double compatibilityScore;
    private Integer matchPercentage;
    private MatchBreakdownDTO breakdown;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String explanation;
    private List<String> recommendations;
    private String matchQuality;
}
