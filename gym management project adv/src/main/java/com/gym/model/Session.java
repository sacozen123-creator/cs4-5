
package com.gym.model;
import jakarta.persistence.*;

@Entity
public class Session {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;
 private String name;
 private String trainer;

 public String getName(){return name;}
 public void setName(String n){this.name=n;}
 public String getTrainer(){return trainer;}
 public void setTrainer(String t){this.trainer=t;}
}
