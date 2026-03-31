package com.example.demo.resource.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.resource.dto.SelectDto;
import com.example.demo.resource.entity.Resource;
import com.example.demo.resource.enums.ResourceStatus;
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
    //给管理员页面返回所有资源
    @GetMapping("/api/admin/list")
    public List<Resource> getAllResource(){
        return resourceService.list();
    }
    //管理员手动添加资源
    @PostMapping("/api/admin/add")
    public Result addResource(@RequestBody Resource resource){
        if(resourceService.save(resource)) return Result.success();
        else return Result.error("添加失败");
    }
    //管理员手动停用资源
    @PostMapping("/api/admin/disable")
    public Result disableResource(@RequestBody String id){
        if(resourceService.updateResourceStatusById(id,ResourceStatus.UNAVAILABLE)) return Result.success();
        else return Result.error("停用失败");
    }
    //管理员手动启用资源
    @PostMapping("/api/admin/enable")
    public Result enableResource(@RequestBody String id){
        if(resourceService.updateResourceStatusById(id,ResourceStatus.AVAILABLE)) return Result.success();
        else return Result.error("启用失败");
    }
}
