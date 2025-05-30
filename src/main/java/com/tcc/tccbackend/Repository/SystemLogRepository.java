package com.tcc.tccbackend.Repository;

import com.tcc.tccbackend.Model.SystemLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLogRepository extends MongoRepository<SystemLog, String> {
}