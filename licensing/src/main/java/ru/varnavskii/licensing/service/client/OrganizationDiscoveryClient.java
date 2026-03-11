package ru.varnavskii.licensing.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.varnavskii.licensing.model.Organization;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrganizationDiscoveryClient {

    private final DiscoveryClient discoveryClient;

    public Organization getOrganization(String id) {
        RestTemplate restTemplate = new RestTemplate();

        List<ServiceInstance> instances = discoveryClient.getInstances("organization-service");

        if (instances.isEmpty()) {
            return null;
        }
        String serviceUrl = String.format("%s/v1/organization/%s",
                instances.get(0).getUri().toString(), id);

        ResponseEntity<Organization> restExchange = restTemplate.exchange(
                serviceUrl, HttpMethod.GET, null, Organization.class, id
        );

        return restExchange.getBody();
    }
}
