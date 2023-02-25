package com.example.creditmarket.openAPI;


import com.example.creditmarket.entity.EntityFProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlingRepositoryFProduct extends JpaRepository<EntityFProduct, String> {

}
