package com.example.notesharing.Repository;

import com.example.notesharing.modal.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {

    List<Advertisement> findByActiveTrue();
}
