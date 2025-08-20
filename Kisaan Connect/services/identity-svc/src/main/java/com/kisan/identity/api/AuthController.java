package com.kisan.identity.api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth/otp")
public class AuthController {
  @PostMapping("/send")
  public ResponseEntity<?> send(@RequestBody Map<String,String> body){
    return ResponseEntity.accepted().build();
  }
  @PostMapping("/verify")
  public Map<String,String> verify(@RequestBody Map<String,String> body){
    return Map.of("access_token","dev.jwt","refresh_token","dev.rjwt");
  }
}

