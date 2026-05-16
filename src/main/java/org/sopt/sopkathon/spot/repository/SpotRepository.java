package org.sopt.sopkathon.spot.repository;

import org.sopt.sopkathon.spot.domain.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotRepository extends JpaRepository<Spot, Long> {
}
