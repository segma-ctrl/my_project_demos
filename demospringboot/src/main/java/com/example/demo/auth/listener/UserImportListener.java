package com.example.demo.auth.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.demo.auth.dto.UserImportDto;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户导入监听器
 */
@Slf4j
public class UserImportListener implements ReadListener<UserImportDto> {

    private final UserService userService;
    private final List<UserImportDto> dataList = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    public UserImportListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void invoke(UserImportDto data, AnalysisContext context) {
        // 逐行读取数据
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 所有数据读取完成后处理
        log.info("所有数据解析完成，共{}条", dataList.size());
    }

    /**
     * 批量保存数据到数据库
     * @return 导入结果信息
     */
    public List<String> saveData() {
        List<String> successIds = new ArrayList<>();

        for (UserImportDto dto : dataList) {
            try {
                // 检查 ID 是否已存在
                User existingUser = userService.getById(dto.getId());
                if (existingUser != null) {
                    errorMessages.add(String.format("ID %s 已存在，跳过导入", dto.getId()));
                    continue;
                }

                // 检查用户名是否已存在
                User existingByUsername = userService.getByUserName(dto.getUsername());
                if (existingByUsername != null) {
                    errorMessages.add(String.format("用户名 %s 已存在，跳过导入", dto.getUsername()));
                    continue;
                }

                // 转换并保存
                User user = convertToUser(dto);
                if (userService.save(user)) {
                    successIds.add(dto.getId());
                } else {
                    errorMessages.add(String.format("用户 %s 保存失败", dto.getUsername()));
                }
            } catch (Exception e) {
                log.error("导入用户 {} 失败：{}", dto.getUsername(), e.getMessage());
                errorMessages.add(String.format("用户 %s 导入失败：%s", dto.getUsername(), e.getMessage()));
            }
        }

        return errorMessages;
    }

    /**
     * 将 DTO 转换为 User 实体
     */
    private User convertToUser(UserImportDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setCreateTime(new Date());
        return user;
    }

}
