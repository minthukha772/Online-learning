 package com.blissstock.mappingSite.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.validation.Valid;

import com.blissstock.mappingSite.dto.StudentReviewDTO;
import com.blissstock.mappingSite.dto.TeacherReviewDTO;
import com.blissstock.mappingSite.entity.CourseInfo;
import com.blissstock.mappingSite.entity.JoinCourseUser;
import com.blissstock.mappingSite.entity.PaymentReceive;
import com.blissstock.mappingSite.entity.Review;
import com.blissstock.mappingSite.entity.UserInfo;
import com.blissstock.mappingSite.enums.UserRole;
import com.blissstock.mappingSite.repository.CourseInfoRepository;
import com.blissstock.mappingSite.repository.JoinCourseUserRepository;
import com.blissstock.mappingSite.repository.ReviewRepository;
import com.blissstock.mappingSite.service.StudentReviewService;
import com.blissstock.mappingSite.service.TeacherReviewService;
import com.blissstock.mappingSite.service.UserService;
import com.blissstock.mappingSite.service.UserSessionService;

import org.hibernate.annotations.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller

public class ReviewController {

    @Autowired
    UserSessionService userSessionService;

    @Autowired
    UserService userService;
    
    @Autowired
    JoinCourseUserRepository joinRepo;

    @Autowired
    ReviewRepository reviewRepo;

    @Autowired
    StudentReviewService stuReviewService;

    @Autowired
    TeacherReviewService trReviewService;
    //get student review 
    @Valid
    @GetMapping(value="/student/student-review/{courseId}")
    private String getStudentReviewForm(@PathVariable Long courseId, Model model) {  

        if(userSessionService.getRole() == UserRole.STUDENT){
          StudentReviewDTO stuReview = new StudentReviewDTO();
          Long userId = userSessionService.getUserAccount().getId();
          model.addAttribute("review", stuReview);
          model.addAttribute("postAction", "/student/update-student-review/"+courseId+"/"+userId);
       }
        
        return "CM0007_WriteReviewStudent";

	}

    @PostMapping(value="/student/update-student-review/{courseId}/{userId}")
    private String postStudentReviewForm( @Valid @ModelAttribute("review") StudentReviewDTO stuReviewDTO, @PathVariable Long courseId, @PathVariable Long userId, BindingResult bindingResult, Model model, @RequestParam(value="action", required=true) String action) { 
        if(bindingResult.hasErrors()) {
			return "CM0007_WriteReviewStudent";			
		}   
        try{
            if(action.equals("submit")){
              stuReviewService.addReview(stuReviewDTO, courseId, userId);
            }
          }catch(Exception e){
            System.out.println(e);
          }
        model.addAttribute("infoMap", stuReviewDTO.toMapReview());
        return "redirect:/student/Review/Complete";
	}
    
    @Valid
    @GetMapping(value="/admin/edit-student-review/{reviewId}")
    private String editStudentReviewForm(@PathVariable Long reviewId, Model model, final RedirectAttributes redirectAttributes) {         
        Review review=reviewRepo.findById(reviewId).orElse(null);
        model.addAttribute("review", review);
        if(userSessionService.getRole() == UserRole.ADMIN){
          model.addAttribute("review", review);
          model.addAttribute("postAction", "/admin/update-student-review/"+reviewId);    
       }
	    return "redirect:/admin/Review/Complete";
	}
  
  @PostMapping(value="/admin/update-student-review/{reviewId}")
    private String adminPostStudentReview( @Valid @ModelAttribute("review") StudentReviewDTO stuReviewDTO, @PathVariable Long reviewId, BindingResult bindingResult, Model model, @RequestParam(value="action", required=true) String action) { 
        if(bindingResult.hasErrors()) {
			return "CM0007_WriteReviewStudent";			
		}   
        try{
            if(action.equals("submit")){
              Review review=reviewRepo.findById(reviewId).orElse(null);
              review.setStar(stuReviewDTO.getStar());
              review.setFeedback(stuReviewDTO.getFeedback());
              reviewRepo.save(review);
            }
          }catch(Exception e){
            System.out.println(e);
          }
        model.addAttribute("infoMap", stuReviewDTO.toMapReview());
        return "CM0007_WriteReviewStudent";
	}
    
    //get teacher review 
    @GetMapping(value="/teacher/teacher-review/{courseId}/{userId}")//userId will be studentId
    private String getTeacherReviewForm(@PathVariable Long courseId, @PathVariable Long userId, Model model) {
        TeacherReviewDTO trReview = new TeacherReviewDTO();
        model.addAttribute("review", trReview);
        model.addAttribute("postAction", "/teacher/update-teacher-review/"+courseId+"/"+userId);
	    return "redirect:/teacher/Review/Complete";
	}
    @PostMapping(value="/teacher/update-teacher-review/{courseId}/{userId}")
    private String postTeacherReviewForm( @Valid @ModelAttribute("review") TeacherReviewDTO trReviewDTO, BindingResult bindingResult,@PathVariable Long courseId, @PathVariable Long userId, Model model, @RequestParam(value="action", required=true) String action) { 
        if(bindingResult.hasErrors()) {
			return "CM0007_WriteReviewTeacher";			
		}  
        try{
            if(action.equals("submit")){
              System.out.println(trReviewDTO.getReviewType());
              trReviewService.addReview(trReviewDTO, courseId, userId);
            }
          }catch(Exception e){
            System.out.println(e);
          }
        model.addAttribute("infoMap", trReviewDTO.toMapTrReview());
        return "CM0007_WriteReviewTeacher";
	}

     //edit teacher review
     @Valid
     @GetMapping(value="/admin/edit-teacher-review/{reviewId}")
     private String editTeacherReviewForm(@PathVariable Long reviewId, Model model, final RedirectAttributes redirectAttributes) {  
         Review review=reviewRepo.findById(reviewId).orElse(null);
         model.addAttribute("review", review);
         if(userSessionService.getRole() == UserRole.ADMIN){
           model.addAttribute("review", review);
           model.addAttribute("postAction", "/admin/update-teacher-review/"+reviewId);    
        }
       return "CM0007_WriteReviewTeacher";
   }
   
   @PostMapping(value="/admin/update-teacher-review/{reviewId}")
     private String adminPostTeacherReview( @Valid @ModelAttribute("review") TeacherReviewDTO trReviewDTO, @PathVariable Long reviewId, BindingResult bindingResult, Model model, @RequestParam(value="action", required=true) String action) { 
         if(bindingResult.hasErrors()) {
       return "CM0007_WriteReviewTeacher";			
     }   
         try{
             if(action.equals("submit")){
               Review review=reviewRepo.findById(reviewId).orElse(null);
               review.setStar(trReviewDTO.getReviewType());
               review.setFeedback(trReviewDTO.getFeedback());
               reviewRepo.save(review);
             }
           }catch(Exception e){
             System.out.println(e);
           }
         model.addAttribute("infoMap", trReviewDTO.toMapTrReview());
         return "redirect:/admin/Review/Complete";
   }
   
   }

    
