package telran.spring.students.service;

import telran.spring.students.dto.*;

public interface StudentsService {
	Student addStudent(Student student);
	void addMark(long studentId, Mark mark);
}
