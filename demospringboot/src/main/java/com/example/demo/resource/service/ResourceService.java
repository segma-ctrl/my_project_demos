package com.example.demo.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.resource.dto.SelectDto;
import com.example.demo.resource.entity.Resource;
import com.example.demo.resource.enums.ResourceStatus;

import java.util.List;

public interface ResourceService extends IService<Resource> {
    List<Resource> getUsableResource(SelectDto selectDto);
    boolean updateResourceStatusById(String id, ResourceStatus status);
}
