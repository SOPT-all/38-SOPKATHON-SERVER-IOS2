package org.sopt.sopkathon.anonymity.repository;

import java.util.Optional;
import org.sopt.sopkathon.anonymity.domain.Anonymity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymityRepository extends JpaRepository<Anonymity, Long> {

    boolean existsByStory_IdAndUser_Id(Long storyId, Long userId);

    Optional<Anonymity> findByStory_IdAndUser_Id(Long storyId, Long userId);
}
