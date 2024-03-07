package com.microsoft.azure.appservice.examples.tomcatmysql.models;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
public class Task implements Serializable { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; 

    @Column(name = "name")
    private String name; 

    public Long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }
} 