package yazlab.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yazlab.domain.User;

public interface UserRepository extends MongoRepository<User, String> {
}
