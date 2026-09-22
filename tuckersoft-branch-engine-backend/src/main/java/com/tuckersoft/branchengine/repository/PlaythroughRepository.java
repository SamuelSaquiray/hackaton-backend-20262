package com.tuckersoft.branchengine.repository;
import com.tuckersoft.branchengine.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PlaythroughRepository extends JpaRepository<Playthrough,Long> {
    boolean existsByPlayerTag(String playerTag);
    List<Playthrough> findByUserOrderByCreatedAtDesc(User user);
    Optional<Playthrough> findByIdAndUser(Long id, User user);
}
