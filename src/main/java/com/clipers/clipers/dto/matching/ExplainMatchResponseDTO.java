package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplainMatchResponseDTO {
    private String candidateId;
    private String jobId;
    private Double compatibilityScore;
    private Integer matchPercentage;
    private MatchBreakdownDTO breakdown;
    private Map<String, String> detailedAnalysis;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private String decisionRecommendation;
}
