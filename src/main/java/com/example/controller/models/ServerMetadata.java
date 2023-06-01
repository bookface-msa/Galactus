package com.example.controller.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "ServerMetadata")
@Table(name = "server_metadata")
public class ServerMetadata {
    @Id
    @Column(name = "ip_addresse")
    public String ipAddresse;

    @Column(name = "name", unique = true)
    public String name;

    @Column(name = "deployment_status")
    public String deploymentStatus;

    @Column(name = "status")
    public byte status = 0;

    @Column(name = "username")
    public String username = "root";

    @Column(name = "password")
    public String password = "mypassword";
}
