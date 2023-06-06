package com.example.controller.models;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "ServiceMetadata")
@Table(name = "service_metadata")
public class ServiceMetadata {
    @Id
    @Column(name = "id", updatable = false)
    public String id;
    
    @Column(name = "name", unique = true)
    public String name;
    
    @Column(name = "port", nullable = false)
    public Integer port;
    
    @Column(name = "maxInstanceCount", nullable = true)
    public Integer maxInstanceCount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "services_resources_table", joinColumns = {
            @JoinColumn(name = "service_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "resource_name", referencedColumnName = "name")
    })
    public Set<ResourceMetadata> resources;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = ServerMetadata.class)
    @JoinColumn(name = "service_id_fk", referencedColumnName = "id")
    public Set<ServerMetadata> servers;
}