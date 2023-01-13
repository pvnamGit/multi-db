package com.multitenantmultidb.multidb.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product {

  @Builder
  public Product(Long id, String name, Integer version) {
    this.id = id;
    this.name = name;
    this.version = version;
  }

  @Id
  @Column(name = "id")
  protected Long id;

  @Column(name = "name")
  @NotNull
  @Size(max = 255)
  private String name;

  @Version
  @Column(name = "version")
  protected Integer version;

}
