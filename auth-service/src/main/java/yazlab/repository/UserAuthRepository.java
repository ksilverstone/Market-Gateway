package yazlab.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import yazlab.domain.UserAuth;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends MongoRepository<UserAuth, String> {
    Optional<UserAuth> findByUsername(String username);
}
