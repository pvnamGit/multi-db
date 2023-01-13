package com.multitenantmultidb.multidb.repository;

import com.multitenantmultidb.multidb.model.Tenant;
import org.springframework.data.repository.CrudRepository;

public interface TenantRepository extends CrudRepository<Tenant, String> {
}
