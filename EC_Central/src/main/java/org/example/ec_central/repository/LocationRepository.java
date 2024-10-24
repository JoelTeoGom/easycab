package org.example.ec_central.repository;

import org.example.ec_central.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByIdentifier(String identifier);

    @Modifying
    void deleteAll();

}