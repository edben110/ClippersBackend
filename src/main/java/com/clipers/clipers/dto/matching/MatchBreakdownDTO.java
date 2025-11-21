package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchBreakdownDTO {
    private Double skillsMatch;
    private Double experienceMatch;
    private Double educationMatch;
    private Double semanticMatch;
    private Double locationMatch;
}
