package in.blackhawkss.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientOrgRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 160) String area,
        @NotBlank @Size(max = 80) String city,
        @Size(max = 40) String sector,
        @Size(max = 300) String highlight,
        Boolean published,
        Integer sortOrder
) {}
