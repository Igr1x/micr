package ru.varnavskii.licensing.repositoty;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.varnavskii.licensing.model.License;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends CrudRepository<License, String> {
    List<License> findByOrganizationId(String organizationId);
    Optional<License> findByOrganizationIdAndLicenseId(String organizationId, String licenseId);
}
