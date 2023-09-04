package telran.spring.students.repo;

import java.time.LocalDate;
import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.spring.students.docs.*;
import telran.spring.students.dto.SubjectMark;

public interface StudentRepository extends MongoRepository<StudentDoc, Long> {
SubjectMark findByIdAndMarksSubjectEquals(Long id, String subject);
SubjectMark findByIdAndMarksDateBetween(Long id, LocalDate date1, LocalDate date2);
}
