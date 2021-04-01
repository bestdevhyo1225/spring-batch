package com.hs.batch.repository

import com.hs.batch.entity.Pay
import org.springframework.data.jpa.repository.JpaRepository

interface PayRepository : JpaRepository<Pay, Long>
