package telran.spring.students.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.spring.students.docs.StudentDoc;

public interface StudentRepository extends MongoRepository<StudentDoc, Long> {

}
