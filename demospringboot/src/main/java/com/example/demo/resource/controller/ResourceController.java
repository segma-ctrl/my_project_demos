package com.example.demo.resource.controller;

import com.example.demo.resource.dto.SelectDto;
import com.example.demo.resource.entity.Resource;
import com.example.demo.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;
    //获取所有可用的资源
    @PostMapping("/api/resource")
    public List<Resource> getAllResource(@RequestBody SelectDto selectDto){
        return resourceService.getUsableResource(selectDto);
    }

}
