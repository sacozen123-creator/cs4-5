
package com.gym.model;
import jakarta.persistence.*;

@Entity
public class Booking {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String session;
    private String trainer;

    public Long getId(){ return id; }

    public String getUsername(){ return username; }
    public void setUsername(String u){ this.username = u; }

    public String getSession(){ return session; }
    public void setSession(String s){ this.session = s; }

    public String getTrainer(){ return trainer; }
    public void setTrainer(String t){ this.trainer = t; }
}
