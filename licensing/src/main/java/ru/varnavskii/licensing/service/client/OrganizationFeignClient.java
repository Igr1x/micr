package ru.varnavskii.licensing.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.varnavskii.licensing.model.Organization;

@FeignClient("organization-service")
public interface OrganizationFeignClient {
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/v1/organization/{id}",
            consumes = "application/json"
    )
    Organization getOrganization(@PathVariable("id") String id);
}
