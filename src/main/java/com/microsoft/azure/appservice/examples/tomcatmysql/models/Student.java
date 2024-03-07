package com.microsoft.azure.appservice.examples.tomcatmysql.models;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
public class Student implements Serializable { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; 

    @Column(name = "name")
    private String name; 

    @Column(name = "std")
    private int std; 
      
    public Long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public int getStd() { 
        return std; 
    }

    public void setStd(int std) { 
        this.std = std; 
    } 
} 