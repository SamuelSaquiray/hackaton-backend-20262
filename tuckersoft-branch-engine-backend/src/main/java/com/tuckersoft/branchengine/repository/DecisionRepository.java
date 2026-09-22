package com.tuckersoft.branchengine.repository;
import com.tuckersoft.branchengine.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DecisionRepository extends JpaRepository<Decision,Long> {
    List<Decision> findByPlaythroughOrderByCreatedAtAsc(Playthrough p);
    List<Decision> findByPlaythroughInOrderByCreatedAtAsc(Collection<Playthrough> p);
    Page<Decision> findByPlaythroughIn(Collection<Playthrough> p, Pageable pageable);
}
