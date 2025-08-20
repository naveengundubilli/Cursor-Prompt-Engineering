package com.kisan.profile.api;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/farmers")
public class FarmerController {
  @PostMapping public Map<String,Object> upsert(@RequestBody Map<String,Object> farmer){
    if(!farmer.containsKey("id")) farmer.put("id", UUID.randomUUID().toString());
    return farmer;
  }
  @GetMapping("/{id}") public Map<String,Object> byId(@PathVariable String id){
    return Map.of("id", id, "phone", "+910000000001");
  }
  @GetMapping public List<Map<String,Object>> list(){
    return List.of(Map.of("id","1","phone","+910000000001"));
  }
}

