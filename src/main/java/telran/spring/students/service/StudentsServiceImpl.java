package telran.spring.students.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.exceptions.NotFoundException;
import telran.spring.students.docs.StudentDoc;
import telran.spring.students.dto.Mark;
import telran.spring.students.dto.Student;
import telran.spring.students.dto.SubjectMark;
import telran.spring.students.repo.StudentRepository;
@RequiredArgsConstructor
@Slf4j
@Service
public  class StudentsServiceImpl implements StudentsService {
	final StudentRepository studentRepo;
	@Override
	@Transactional(readOnly = false)
	public Student addStudent(Student student) {
		if (studentRepo.existsById(student.id())) {
			throw new IllegalStateException(String.format("student with id %d already exists", student.id()));
		}
		StudentDoc studentDoc = StudentDoc.of(student);
		Student studentRes = studentRepo.save(studentDoc).build();
		log.trace("Student {} has been added", studentRes);
		return studentRes;
	}

	@Override
	@Transactional(readOnly = false)
	public void addMark(long studentId, Mark mark) {
		StudentDoc studentDoc = studentRepo.findById(studentId).orElseThrow(() ->
		new NotFoundException(String.format("student with id %d doesn't exist", studentId)));
		List<Mark>marks = studentDoc.getMarks();
		marks.add(mark);
		studentRepo.save(studentDoc);
		

	}

	@Override
	public List<Mark> getMarksStudentSubject(long studentId, String subject) {
		List<Mark> res = Collections.emptyList();
		SubjectMark allMarks = studentRepo.findByIdAndMarksSubjectEquals(studentId, subject);
		if(allMarks != null) {
			res = allMarks.getMarks().stream().filter(m -> m.subject().equals(subject)).toList();
		}
		return res;
	}

	@Override
	public List<Mark> getMarksStudentDates(long studentId, LocalDate date1, LocalDate date2) {
		List<Mark> res = Collections.emptyList();
		SubjectMark allMarks = studentRepo.findByIdAndMarksDateBetween(studentId, date1, date2);
		if(allMarks != null) {
			res = allMarks.getMarks().stream().filter(m -> {
				LocalDate date = m.date();
				return date.compareTo(date1) >= 0 && date.compareTo(date2) <= 0;
			}).toList();
		}
		return res;
	}

}
