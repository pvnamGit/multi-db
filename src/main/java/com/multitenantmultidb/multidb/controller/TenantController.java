package com.multitenantmultidb.multidb.controller;

import com.multitenantmultidb.multidb.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TenantController {
  @Autowired
  private TenantService tenantManagementService;

  @PostMapping("/tenants")
  public ResponseEntity<Void> createTenant(@RequestParam String db, @RequestParam String password) {
    tenantManagementService.createTenant(db, password);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
