package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplainMatchRequestDTO {
    private CandidateDTO candidate;
    private JobDTO job;
    private Boolean includeSuggestions = true;
}
