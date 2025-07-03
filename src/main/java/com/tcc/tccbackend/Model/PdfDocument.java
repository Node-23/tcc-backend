package com.tcc.tccbackend.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "pdf_documents")
public class PdfDocument implements Serializable {
    @Id
    private String id;
    private Long saleId;
    private String base64Pdf;
    private String fileName;

    public PdfDocument() {
    }

    public PdfDocument(Long saleId, String base64Pdf, String fileName) {
        this.saleId = saleId;
        this.base64Pdf = base64Pdf;
        this.fileName = fileName;
    }
}