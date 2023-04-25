package com.blissstock.mappingSite.repository;

import com.blissstock.mappingSite.entity.TestQuestion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    @Query(value = "Select * from test_question where test_test_id = :test_id order by test_test_id desc", nativeQuery = true)
    public List<TestQuestion> getQuestionByTest(@Param("test_id") Long test_id);
}
