package telran.spring.students.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.exceptions.NotFoundException;
import telran.spring.students.docs.StudentDoc;
import telran.spring.students.dto.*;

import telran.spring.students.repo.StudentRepository;
@RequiredArgsConstructor
@Slf4j
@Service
public  class StudentsServiceImpl implements StudentsService {
	
	private static final String AVG_SCORE_FIELD = "avgScore";
	final StudentRepository studentRepo;
	final MongoTemplate mongoTemplate;
	@Value("${app.students.mark.good:80}")
	int goodMark;
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

	@Override
	public List<StudentDoc> getStudentsPhonePrefix(String phonePrefix) {
		
		return studentRepo.findStudentsPhonePrefix(phonePrefix);
	}

	@Override
	public List<IdName> getSudentsAllScoresGreater(int score) {
		
		return studentRepo.findStudentsAllMarksGreater(score);
	}

	@Override
	public List<Long> removeStudentsWithFewMarks(int nMarks) {
		List<StudentDoc> studentsRemoved = studentRepo.removeStudentsFewMarks(nMarks);
		return studentsRemoved.stream().map(StudentDoc::getId).toList();
	}

	@Override
	public double getStudentsAvgScore() {
		UnwindOperation unwindOperation = unwind("marks");
		GroupOperation groupOperation = group().avg("marks.score").as(AVG_SCORE_FIELD);
		Aggregation pipeLine = newAggregation(List.of(unwindOperation, groupOperation));
		var aggregationResult = mongoTemplate.aggregate(pipeLine, StudentDoc.class,Document.class);
		double res = aggregationResult.getUniqueMappedResult().getDouble(AVG_SCORE_FIELD);
		return res;
	}

	@Override
	public List<IdName> getGoodStudents() {
		log.debug("good mark threshold is {} ", goodMark);
		return getStudentsAvgMarkGreater(goodMark);
	}
	@Override
	public List<IdName> getStudentsAvgMarkGreater(int score) {
		UnwindOperation unwindOperation = unwind("marks");
		GroupOperation groupOperation = group("id","name").avg("marks.score").as(AVG_SCORE_FIELD);
		MatchOperation matchOperation = match(Criteria.where(AVG_SCORE_FIELD).gt(score));
		SortOperation sortOperation = sort(Direction.DESC, AVG_SCORE_FIELD);
		ProjectionOperation projectionOperation = project().andExclude(AVG_SCORE_FIELD);
		Aggregation pipeLine = newAggregation(List.of(unwindOperation, groupOperation, matchOperation
				,sortOperation,projectionOperation));
		var aggregationResult = mongoTemplate.aggregate(pipeLine, StudentDoc.class,Document.class);
		List<Document> resultDocuments = aggregationResult.getMappedResults();
		return resultDocuments.stream().map(this::toIdName).toList();
	}
	IdName toIdName(Document document) {
		return new IdName() {
			Document idDocument = document.get("_id", Document.class);
			@Override
			public String getName() {
				
				return idDocument.getString("name");
			}
			
			@Override
			public long getId() {
				
				return idDocument.getLong("id");
			}
		};
	}

}
