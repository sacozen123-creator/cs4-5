
package com.gym.repository;
import com.gym.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepo extends JpaRepository<Booking,Long>{
 List<Booking> findByUsername(String username);
}
