package com.hs.batch.repository

import com.hs.batch.entity.ProductBackup
import org.springframework.data.jpa.repository.JpaRepository

interface ProductBackupRepository : JpaRepository<ProductBackup, Long>
