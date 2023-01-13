package com.multitenantmultidb.multidb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tenant {

  @Id
  @Column(name = "tenant_id")
  private String tenantId;

  @Column(name = "db")
  private String db;

  @Column(name = "password")
  private String password;

}
