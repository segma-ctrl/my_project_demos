package com.example.demo.resource.controller;

import com.example.demo.resource.entity.Resource;
import com.example.demo.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;
    //获取所有资源
    @GetMapping("/api/resource")
    public List<Resource> getAllResource(){
        return resourceService.list();
    }

}
