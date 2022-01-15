package com.blissstock.mappingSite.controller;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.blissstock.mappingSite.entity.JoinCourseUser;
import com.blissstock.mappingSite.dto.StuPaymentDTO;
import com.blissstock.mappingSite.entity.CourseInfo;
import com.blissstock.mappingSite.entity.PaymentReceive;
import com.blissstock.mappingSite.entity.UserInfo;
import com.blissstock.mappingSite.enums.PaymentStatus;
import com.blissstock.mappingSite.enums.UserRole;
import com.blissstock.mappingSite.exceptions.UnauthorizedFileAccessException;
import com.blissstock.mappingSite.model.FileInfo;
import com.blissstock.mappingSite.repository.CourseInfoRepository;
import com.blissstock.mappingSite.repository.JoinCourseUserRepository;
import com.blissstock.mappingSite.repository.PaymentRepository;
//import com.blissstock.mappingSite.repository.CourseTestingRepository;
//import com.blissstock.mappingSite.repository.TpaymentRepository;
import com.blissstock.mappingSite.repository.UserRepository;
import com.blissstock.mappingSite.service.StorageService;
import com.blissstock.mappingSite.service.StorageServiceImpl;
import com.blissstock.mappingSite.service.UserService;
import com.blissstock.mappingSite.service.UserSessionService;
import com.blissstock.mappingSite.utils.CheckUploadFileType;
import com.blissstock.mappingSite.utils.FileNameGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class PaymentController {
  private static Logger logger = LoggerFactory.getLogger(
    PaymentController.class
  );

  @Autowired
  StorageService storageService;

  @Autowired
  public PaymentController(StorageService storageService) {
    this.storageService = storageService;
  }
    
    @Autowired
    UserSessionService userSessionService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepo;

    @Autowired
    PaymentRepository paymentRepo;

    @Autowired
    CourseInfoRepository courseRepo;

    @Autowired
    JoinCourseUserRepository joinRepo;


    /*get student payment screen */
    @Valid
    @GetMapping(value={"/admin/payment-upload/course_id/{courseId}/student_id/{id}",
                      "/student/payment-upload/course_id/{courseId}"})
    private String getPaymentUploadForm(@PathVariable Long courseId,@PathVariable(name = "id", required = false) Long id,Model model) {
		//StuPaymentDTO payment = new StuPaymentDTO();
    Long userId = id==null ? userSessionService.getUserAccount().getAccountId(): id;

    CourseInfo courseInfo=courseRepo.findById(courseId).orElse(null);

    JoinCourseUser join=joinRepo.findByPayment(courseId, userId);
    PaymentReceive payment=paymentRepo.findByJoin(join.getJoinId());
    if(payment==null){
      payment = new PaymentReceive();
    }
  
        model.addAttribute("courseName", courseInfo.getCourseName());
        model.addAttribute("fees", courseInfo.getFees());
        model.addAttribute("payment", payment);
        //Load Profile
        try {
        FileInfo slipPic = storageService.loadSlipAsFileInfo(payment);
        model.addAttribute("slip", slipPic);
        } catch (Exception e) {
        e.printStackTrace();
        logger.info("unable to get payment slip {}", userId);
    }
        return "AS0001_Payment";
    }

    /*student upload payment ss and go to payment success screen */
    /*save ss in image folder and will appear in admin payment check screen */
    @PostMapping(value = {"/admin/payment-upload/course_id/{courseId}/student_id/{id}/edit/",
                         "/student/payment-upload/course_id/{courseId}/edit/"})
    public String savePhoto(
    @Valid @ModelAttribute("payment") PaymentReceive inputPayment,
    BindingResult bindingResult,
    Model model, 
    @RequestParam("slip") MultipartFile slip,  
    @PathVariable Long courseId, 
    @PathVariable Long id,
    HttpServletRequest request)  {
      Long userId = id==null ? userSessionService.getUserAccount().getAccountId(): id;

      JoinCourseUser join=joinRepo.findByPayment(courseId, userId);
      PaymentReceive payment=paymentRepo.findByJoin(join.getJoinId());
      if(payment==null){
        payment = new PaymentReceive();
      }
        payment.setJoin(join);
        joinRepo.save(join);

     
    String redirectAddress =
    "redirect:" +
    request.getRequestURI().replace("/edit/", "");
    logger.debug("Redirect Addresss {}", redirectAddress);

    if (!slip.isEmpty() && CheckUploadFileType.checkType(slip)) {
      //get original photo name and generate a new file name
      String saveFileName = StringUtils.cleanPath(
        slip.getOriginalFilename()
      );

      //upload photo
      try {
        storageService.store(payment.getPaymentReceiveId(), slip, StorageServiceImpl.SLIP_PATH, true);
      } catch (UnauthorizedFileAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      //insert photo 
      payment.setSlip(saveFileName);
      if(userSessionService.getUserAccount().getRole()=="ROLE_STUDENT"){
      payment.setPaymentStatus(PaymentStatus.PENDING.getValue());
      }
      
      payment.setPaymentStatus(inputPayment.getPaymentStatus());
      System.out.println(inputPayment.getPaymentStatus());

      payment.setPaymentReceiveDate(GregorianCalendar.getInstance().getTime());
      paymentRepo.save(payment);

      logger.info("payment slip {} stored", saveFileName);
    }
    if(payment.getPaymentStatus()=="ERROR"){
      return redirectAddress;
    }
      return "redirect:/payment/complete";
  
    }

  
//     @GetMapping(value="/edit-payment-slip/{paymentReceiveId}")
//     private String stuGetPaymentSlip(@PathVariable Long paymentReceiveId,Model model) {
//       PaymentReceive paymentInfo= paymentRepo.findById(paymentReceiveId).orElse(null);
//       //PaymentTesting payment = new PaymentTesting();
//       model.addAttribute("payment", paymentInfo);
//       model.addAttribute("error", paymentInfo);
//       return "AS0001_StudentPayment";
// 	}
//     /*get admin payment check screen */
//     @GetMapping(value="/admin-check-stupayment/{paymentReceiveId}")
//     private String adminGetStudentPaymentSlip(@PathVariable Long paymentReceiveId,Model model) {
//         PaymentReceive paymentInfo= paymentRepo.findById(paymentReceiveId).orElse(null);
//         model.addAttribute("payment", paymentInfo);
//         model.addAttribute("error", paymentInfo);
// 	return "AS0001_AdminPaymentCheck";
// 	}
//   /*@PostMapping("/update-stupayment-status") 
//   public String updatePaymentStatus(@RequestParam("paymentReceiveId") Long paymentReceiveId, @Valid @ModelAttribute("payment") PaymentTesting paymentInfo, BindingResult result, Model model,RedirectAttributes redirectAttr) { 
//   PaymentTesting payment= paymentRepo.findById(paymentReceiveId).orElse(null);
//   //review.setStar(reviewInfo.getStar());
//   //review.setFeedback(reviewInfo.getFeedback());
//   paymentRepo.save(payment);
//   return "redirect:/student-review?reviewId="+paymentReceiveId;
 
//  }
//  30.11.2021*/ 

//     /*admin click submit button and go to payment complete screen . payment status-> complete */
//  @PostMapping("/update-stupayment-status") 
//  /*private String adminUpdatePaymentForm(PaymentTesting inputSlip,Model model,
//  @Valid 
//  BindingResult bindingResult,
//  HttpServletRequest request){*/
//   /*private String updatePaymentStatus(@RequestParam("paymentReceiveId") Long paymentReceiveId, @Valid @ModelAttribute("payment") PaymentTesting paymentInfo, BindingResult result, Model model,RedirectAttributes redirectAttr) { */
//     private String updatePaymentStatus(@RequestParam("paymentReceiveId") Long paymentReceiveId, @Valid @ModelAttribute("payment") PaymentReceive paymentInfo, HttpServletRequest request, BindingResult result, Model model) {
//     PaymentReceive payment= paymentRepo.findById(paymentReceiveId).orElse(null);
//     payment.setPaymentStatus("Complete");
//     paymentRepo.save(payment);
//     return "AdminPaymentCheckSuccess";
//  }

//  /*admin click request to reupload button and modal appear and go to payment error screen . payment status-> error */
//  @PostMapping("/payment-error-reason") 
//  private String updatePaymentError(@RequestParam("paymentReceiveId") Long paymentReceiveId,@RequestParam("paymentErrStatus") String paymentErrStatus, @Valid @ModelAttribute("error") PaymentReceive paymentInfo, HttpServletRequest request, BindingResult result, Model model){
  
//   PaymentReceive errorReason= paymentRepo.findById(paymentReceiveId).orElse(null);
//   //TODO inspect here
//   //errorReason.setPaymentErrStatus(paymentErrStatus);
//   errorReason.setPaymentStatus("Error");;
//   paymentRepo.save(errorReason);
//   return "AdminPaymentCheckError";
//  }
//  private Long getUid(Long id) {
//   Long uid = 0L;
//   UserRole role = userSessionService.getRole();
//   if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
//     uid = id;
//   } else if (id != null) {
//     uid = id;
//   } else if (role == UserRole.TEACHER || role == UserRole.STUDENT) {
//     uid = userSessionService.getUserAccount().getAccountId();
//   } else {
//     throw new RuntimeException("user authetication fail");
//   }
//   return uid;
// }
}
