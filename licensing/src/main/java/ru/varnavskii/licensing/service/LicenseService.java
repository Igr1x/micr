package ru.varnavskii.licensing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import ru.varnavskii.licensing.config.ServiceConfig;
import ru.varnavskii.licensing.model.License;
import ru.varnavskii.licensing.model.Organization;
import ru.varnavskii.licensing.repositoty.LicenseRepository;
import ru.varnavskii.licensing.service.client.OrganizationDiscoveryClient;
import ru.varnavskii.licensing.service.client.OrganizationFeignClient;
import ru.varnavskii.licensing.service.client.OrganizationRestTemplateClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final ResourceBundleMessageSource resourceBundleMessageSource;
    private final LicenseRepository licenseRepository;
    private final ServiceConfig config;

    private final OrganizationDiscoveryClient organizationDiscoveryClient;
    private final OrganizationRestTemplateClient organizationRestClient;
    private final OrganizationFeignClient organizationFeignClient;

    public License getLicense(String licenseId, String organizationId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(resourceBundleMessageSource.getMessage("license.search.error.message",
                        new Object[]{}, null), licenseId, organizationId)));
        return license.withComment(config.getProperty());
    }

    public License getLicense(String licenseId, String organizationId, String clientType){
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId)
                .orElseThrow();
        if (null == license) {
            throw new IllegalArgumentException(String.format(resourceBundleMessageSource.getMessage("license.search.error.message", null, null),licenseId, organizationId));
        }

        Organization organization = retrieveOrganizationInfo(organizationId, clientType);
        if (null != organization) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license.withComment(config.getProperty());
    }

    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestClient.getOrganization(organizationId);
                break;
        }

        return organization;
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
