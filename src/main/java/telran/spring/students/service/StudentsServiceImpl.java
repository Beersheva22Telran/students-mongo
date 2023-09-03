package telran.spring.students.service;

import org.springframework.transaction.annotation.Transactional;

import telran.spring.students.dto.Mark;
import telran.spring.students.dto.Student;

public class StudentsServiceImpl implements StudentsService {

	@Override
	@Transactional(readOnly = false)
	public Student addStudent(Student student) {
		
		return null;
	}

	@Override
	public void addMark(long studentId, Mark mark) {
		// TODO Auto-generated method stub

	}

}
