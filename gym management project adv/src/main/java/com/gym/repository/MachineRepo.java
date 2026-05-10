package com.gym.repository;

import com.gym.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MachineRepo extends JpaRepository<Machine, Long> {
    List<Machine> findByAssignedTrainer(String trainer);
}