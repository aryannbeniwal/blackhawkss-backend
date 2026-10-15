package in.blackhawkss.api.dto.response;

import in.blackhawkss.api.domain.ClientOrg;

public record ClientOrgResponse(Long id, String name, String area, String city, String sector, String highlight,
                                boolean published, int sortOrder) {
    public static ClientOrgResponse from(ClientOrg c) {
        return new ClientOrgResponse(c.getId(), c.getName(), c.getArea(), c.getCity(), c.getSector(), c.getHighlight(),
                c.isPublished(), c.getSortOrder());
    }
}
