package org.opennms.web.rest.v2.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SourceNameDto {
    private Long id;
    private String name;

    public SourceNameDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public static List<SourceNameDto> fromEntity(Map<Long, String> idToNameMap) {
        return idToNameMap.entrySet().stream()
                .map(entry -> new SourceNameDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
