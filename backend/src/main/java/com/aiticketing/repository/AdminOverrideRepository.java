package com.aiticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiticketing.entity.AdminOverride;

public interface AdminOverrideRepository extends JpaRepository<AdminOverride, Long> {
}