package com.tcc.tccbackend.Repository;

import com.tcc.tccbackend.Model.PdfDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PdfDocumentRepository extends MongoRepository<PdfDocument, String> {
    Optional<PdfDocument> findBySaleId(Long saleId);
}