package com.blissstock.mappingSite.repository;

import java.util.List;

import com.blissstock.mappingSite.entity.CourseInfo;
import com.blissstock.mappingSite.entity.Syllabus;

import org.hibernate.annotations.Parameter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SyllabusRepository extends CrudRepository<Syllabus, Long>{


    List<Syllabus> findByCourseInfo(CourseInfo courseInfo);

    
}
