package telran.spring.students.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import telran.spring.students.repo.StudentRepository;

@Component
@RequiredArgsConstructor
public class RandomDBCreation {
	 final StudentRepository studentRepo;
	 @Value("${app.random.students.amount:100}")
     int nStudents;
	 @Value("${app.random.subjects.amount:5}")
     int nSubjects;
	 @Value("${app.random.marks.amount.min:3}")
     int minMarks;
	 @Value("${app.random.marks.amount.max:7}")
     int maxMarks;
	 @Value("${app.random.creation.enable:false}")
     boolean creationEnable;
}
