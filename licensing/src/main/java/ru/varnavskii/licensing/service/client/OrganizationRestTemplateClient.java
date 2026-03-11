package ru.varnavskii.licensing.service.client;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.varnavskii.licensing.model.Organization;

@Component
@RequiredArgsConstructor
public class OrganizationRestTemplateClient {

    private final RestTemplate restTemplate;

    public Organization getOrganization(String id) {
        return restTemplate.exchange(
                "http://organization-service/v1/organization/{id}",
                HttpMethod.GET, null, Organization.class, id
        ).getBody();
    }
}
