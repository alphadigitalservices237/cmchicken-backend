package com.limiter.demo.payment;

import com.limiter.demo.models.Receipt;
import com.limiter.demo.models.UserEntity;
import com.limiter.demo.repositories.ReceiptRepository;
import com.limiter.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/auth/receipts")
public class ReceiptController {
    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("user/connected/receipts")
    public Object getConnectedUserReceipt()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> connectedUser = userRepository.findByUsername(authentication.getName());
        if(connectedUser.isPresent())
        {
            long user_id = connectedUser.get().getId();
            List<Receipt> receipts = receiptRepository.findAll();
            List<Receipt> connectedUserReceipts =  receipts.parallelStream().filter(receipt -> receipt.getUser_id()==user_id).collect(Collectors.toList());
            return new ResponseEntity<>(connectedUserReceipts, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("PLEASE LOGIN",HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping("users/receipts/all")
    public Object getAllReceipts()
    {
        return new ResponseEntity<>(receiptRepository.findAll().parallelStream().collect(Collectors.toList()),HttpStatus.OK);
    }
}
