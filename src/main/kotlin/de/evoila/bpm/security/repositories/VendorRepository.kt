package de.evoila.bpm.security.repositories

import de.evoila.bpm.security.model.Vendor
import org.springframework.data.jpa.repository.JpaRepository

interface VendorRepository : JpaRepository<Vendor, Long>