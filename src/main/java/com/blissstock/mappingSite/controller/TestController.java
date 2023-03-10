package com.blissstock.mappingSite.controller;

import java.util.List;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.ui.Model;

import com.blissstock.mappingSite.entity.CourseInfo;
import com.blissstock.mappingSite.entity.Test;
import com.blissstock.mappingSite.entity.UserInfo;
import com.blissstock.mappingSite.enums.UserRole;
import com.blissstock.mappingSite.repository.CourseInfoRepository;
import com.blissstock.mappingSite.repository.TestRepository;
import com.blissstock.mappingSite.repository.UserInfoRepository;
import com.blissstock.mappingSite.service.UserSessionService;

@Controller
public class TestController {

    @Autowired
    UserSessionService userSessionService;

    @Autowired
    TestRepository testRepository;

    @Autowired
    CourseInfoRepository courseInfoRepository;

    @Autowired
    UserInfoRepository userInfoRepository;

    @Valid
    @GetMapping(value = { "/teacher/exam" })
    private String getExamManagementPage(@PathVariable(required = false) Long userId,
            @PathVariable(required = false) String examStatus,
            Model model) {
        if (examStatus == null) {
            examStatus = "";
        }
        Long userID = getUid(null);
        List<Test> testList;
        List<CourseInfo> courseList;

        if (examStatus == "") {
            testList = testRepository.getListByUser(userID);
        } else {
            testList = testRepository.getListByStatusandUser(examStatus, userID);
        }
        courseList = courseInfoRepository.findByUID(userID);

        model.addAttribute("testList", testList);
        model.addAttribute("courseList", courseList);

        return "AT0004_ExamList";
    }

    @Valid
    @PostMapping(value = { "/teacher/create-exam" })
    private ResponseEntity saveExam(@RequestBody String payload) {
        Long userID = getUid(null);
        JSONObject jsonObject = new JSONObject(payload);
        String description = jsonObject.getString("description");
        String section_name = jsonObject.getString("section_name");
        String date = jsonObject.getString("date");
        String exam_status = jsonObject.getString("exam_status");
        Long course_id = jsonObject.getLong("course_id");
        String exam_start_time = jsonObject.getString("start_time");
        String exam_end_time = jsonObject.getString("end_time");
        int passing_score = Integer.parseInt(jsonObject.getString("passing_score"));
        int minutes_allowed = jsonObject.getInt("minutes_allowed");
        CourseInfo courseInfo = courseInfoRepository.findByCourseID(course_id);
        UserInfo userInfo = userInfoRepository.findStudentById(userID);
        Test test = new Test(courseInfo, userInfo, description, section_name, minutes_allowed, passing_score, date,
                exam_start_time, exam_end_time, exam_status);
        testRepository.save(test);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private Long getUid(Long id) {
        Long uid = 0L;
        UserRole role = userSessionService.getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            uid = id;
        } else if (id != null) {
            uid = id;
        } else if (role == UserRole.TEACHER || role == UserRole.STUDENT) {
            uid = userSessionService.getUserAccount().getAccountId();
        } else {
            throw new RuntimeException("user authetication fail");
        }
        return uid;
    }
}
