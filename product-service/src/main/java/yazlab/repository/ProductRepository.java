package yazlab.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yazlab.domain.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}
