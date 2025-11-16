package com.clipers.clipers.dto.matching;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationDTO {
    private String degree;
    private String institution;
    private String field;
    private Integer startYear;
    private Integer endYear;
}
