// com.KDT.mosi.domain.product.repository.ProductDocumentRepository.java

package com.KDT.mosi.domain.product.repository;

import com.KDT.mosi.domain.product.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {

}