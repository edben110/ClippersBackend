package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchMatchResponseDTO {
    private String jobId;
    private String jobTitle;
    private Integer totalCandidates;
    private List<RankedMatchResultDTO> matches;
    private Double averageScore;
    private List<String> topSkillsMatched;
}
