package com.blissstock.mappingSite.controller;

// import java.sql.Date;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.blissstock.mappingSite.entity.CourseInfo;
import com.blissstock.mappingSite.entity.PaymentLists;
import com.blissstock.mappingSite.entity.PaymentReceive;
import com.blissstock.mappingSite.entity.UserInfo;
import com.blissstock.mappingSite.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;

// import com.blissstock.mappingSite.entity.PaymentReceive;
// import com.blissstock.mappingSite.repository.PaymentRepository;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

// import java.util.List;

@Controller
public class PaymentListController {
    
    @Autowired
    PaymentRepository paymentRepo;
    
    
    @RequestMapping("/PaymentList")
    public String PaymentList(Model model)
    {
        List<PaymentReceive> viewPayment = paymentRepo.findAll();
        List<PaymentLists> payUserList = new ArrayList<>();
        // List<AllPaymentLists> allPayment = new AllPaymentLists();
        for(PaymentReceive paymentReceive:viewPayment)
        {
            Date paymentDate = paymentReceive.getPaymentReceiveDate();
            String paymentStatus = paymentReceive.getPaymentStatus();
            UserInfo payUserInfo = paymentReceive.getUserInfo();
            String userName = payUserInfo.getUserName();
            CourseInfo payCouresInfo = paymentReceive.getCourseInfo();
            String courseName = payCouresInfo.getCourseName();
            int courseFees = payCouresInfo.getFees();
            payUserList.add(new PaymentLists(paymentDate, paymentStatus, userName, courseName, courseFees));
        }
        // System.out.println("All Payments"+payUserList);
        model.addAttribute("allPaymentList",payUserList);
        return "AD0003_PaymentListScreen";
    }
}