package com.example.demo.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.client.BookingFeignClient;
import com.example.demo.resource.dto.SelectDto;
import com.example.demo.resource.entity.Resource;
import com.example.demo.resource.enums.ResourceStatus;
import com.example.demo.resource.mapper.ResourceMapper;
import com.example.demo.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Autowired
    private BookingFeignClient bookingFeignClient;

    @Override
    public List<Resource> getUsableResource(SelectDto selectDto) {
        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
        if (selectDto.getType() != null) {
            queryWrapper.eq(Resource::getType, selectDto.getType());
        }
        queryWrapper.eq(Resource::getStatus, ResourceStatus.AVAILABLE);

        List<String> resourceIds = bookingFeignClient.conflictingResourceIds(
                selectDto.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                selectDto.getStartTime().format(DateTimeFormatter.ISO_LOCAL_TIME),
                selectDto.getEndTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
        );
        if (resourceIds != null && !resourceIds.isEmpty()) {
            queryWrapper.notIn(Resource::getId, resourceIds);
        }
        return this.list(queryWrapper);
    }

    @Override
    public boolean updateResourceStatusById(String resourceId, ResourceStatus status) {
        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setStatus(status);
        return this.updateById(resource);
    }
}
