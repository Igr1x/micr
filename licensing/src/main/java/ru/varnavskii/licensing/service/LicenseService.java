package ru.varnavskii.licensing.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import ru.varnavskii.licensing.config.ServiceConfig;
import ru.varnavskii.licensing.model.License;
import ru.varnavskii.licensing.model.Organization;
import ru.varnavskii.licensing.repositoty.LicenseRepository;
import ru.varnavskii.licensing.service.client.OrganizationDiscoveryClient;
import ru.varnavskii.licensing.service.client.OrganizationFeignClient;
import ru.varnavskii.licensing.service.client.OrganizationRestTemplateClient;
import ru.varnavskii.licensing.utils.UserContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class LicenseService {

    private final ResourceBundleMessageSource resourceBundleMessageSource;
    private final LicenseRepository licenseRepository;
    private final ServiceConfig config;

    private final OrganizationDiscoveryClient organizationDiscoveryClient;
    private final OrganizationRestTemplateClient organizationRestClient;
    private final OrganizationFeignClient organizationFeignClient;

    @CircuitBreaker(name = "licenseService",
            fallbackMethod = "buildFallbackLicenseList")
    @Bulkhead(name = "bulkheadLicenseService",
            fallbackMethod = "buildFallbackLicenseList",
            type = Bulkhead.Type.THREADPOOL)
    @Retry(name = "retryLicenseService",
            fallbackMethod = "buildFallbackLicenseList")
    @RateLimiter(name = "licenseService",
            fallbackMethod = "buildFallbackLicenseList")
    public List<License> getByOrganizationId(String organizationId) {
        log.info("LicenseService correlation id {}", UserContextHolder.getContext().getCorrelationId());
        return licenseRepository.findByOrganizationId(organizationId);
    }

    private List<License> buildFallbackLicenseList(String organizationId, Throwable t) {
        List<License> fallbackList = new ArrayList<>();
        License license = new License();
        license.setLicenseId("0000000-00-00000");
        license.setOrganizationId(organizationId);
        license.setProductName("Sorry no licensing information currently available");
        fallbackList.add(license);
        return fallbackList;
    }

    private void randomlyRunLong() throws TimeoutException {
        sleep();
    }

    private void sleep() throws TimeoutException {
        try {
            Thread.sleep(1000);
            throw new TimeoutException();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public License getLicense(String licenseId, String organizationId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(resourceBundleMessageSource.getMessage("license.search.error.message",
                        new Object[]{}, null), licenseId, organizationId)));
        return license.withComment(config.getProperty());
    }

    public License getLicense(String licenseId, String organizationId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                String.format(
                                        resourceBundleMessageSource.getMessage(
                                                "license.search.error.message",
                                                null,
                                                null
                                        ),
                                        licenseId,
                                        organizationId)));

        Organization organization = retrieveOrganizationInfo(organizationId, clientType);
        if (null != organization) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license.withComment(config.getProperty());
    }

    @CircuitBreaker(name = "organizationService")
    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        return switch (clientType) {
            case "feign" -> {
                System.out.println("I am using the feign client");
                yield organizationFeignClient.getOrganization(organizationId);
            }
            case "rest" -> {
                System.out.println("I am using the rest client");
                yield organizationRestClient.getOrganization(organizationId);
            }
            case "discovery" -> {
                System.out.println("I am using the discovery client");
                yield organizationDiscoveryClient.getOrganization(organizationId);
            }
            default -> organizationRestClient.getOrganization(organizationId);
        };
    }

    public License createLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);

        return license.withComment(config.getProperty());
    }

    public License updateLicense(License license) {
        licenseRepository.save(license);

        return license.withComment(config.getProperty());
    }

    public String deleteLicense(String licenseId) {
        License license = new License();
        license.setLicenseId(licenseId);
        licenseRepository.delete(license);
        return String.format(resourceBundleMessageSource.getMessage("license.delete.message",
                new Object[]{}, null), licenseId);

    }


}
