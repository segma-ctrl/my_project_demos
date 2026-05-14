package com.example.demo.auth.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.example.demo.auth.enums.UserRole;

public class UserRoleConverter implements Converter<UserRole> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return UserRole.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public UserRole convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration){
        String value = cellData.getStringValue();
        for(UserRole role:UserRole.values()){
            if(role.getDescription().equals(value)){
                return role;
            }
        }
        return null;
    }
}