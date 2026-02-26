package com.musmike.familyheritage.repository;

import com.musmike.familyheritage.model.AttributeDefinition;
import com.musmike.familyheritage.model.AttributeDictionaryValue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AttributeDictionaryValueRepository extends JpaRepository<AttributeDictionaryValue, Long> {
    Optional<AttributeDictionaryValue> findByAttributeDefinitionAndCode(AttributeDefinition definition, String code);
}
