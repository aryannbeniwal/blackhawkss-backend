package in.blackhawkss.api.web.rest.admin;

import in.blackhawkss.api.dto.request.ClientOrgRequest;
import in.blackhawkss.api.dto.response.ClientOrgResponse;
import in.blackhawkss.api.service.ClientOrgService;
import in.blackhawkss.api.web.rest.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin · Clients")
@RestController
@RequestMapping("/api/v1/admin/clients")
@RequiredArgsConstructor
public class AdminClientController {

    private final ClientOrgService clients;

    @GetMapping
    public ApiResponse<List<ClientOrgResponse>> list() {
        return ApiResponse.ok(clients.listAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClientOrgResponse>> create(@Valid @RequestBody ClientOrgRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(clients.create(req)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ClientOrgResponse> update(@PathVariable Long id, @Valid @RequestBody ClientOrgRequest req) {
        return ApiResponse.ok(clients.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Boolean>> delete(@PathVariable Long id) {
        clients.delete(id);
        return ApiResponse.ok(Map.of("deleted", true));
    }
}
