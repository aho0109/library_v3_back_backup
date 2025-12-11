package org.matsuzaka.library_v3_back.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.matsuzaka.library_v3_back.dto.loanDTO.BookCopyRespDto;
import org.matsuzaka.library_v3_back.model.entity.BookCopy;

@Mapper(componentModel = "spring")
public interface BookCopyMapper {

    BookCopyMapper INSTANCE = Mappers.getMapper(BookCopyMapper.class);

    // 定義 BookCopy 實體到 BookCopyDto 的映射規則
    @Mapping(source = "status", target = "statusDescription", qualifiedByName = "mapStatusToChinese") // 使用 Named 方法進行狀態轉換
    BookCopyRespDto toBookCopyRespDto(BookCopy bookCopy);

    // 將狀態 ENUM 轉換為中文描述
    @Named("mapStatusToChinese")
    default String mapStatusToChinese(Enum<?> statusEnum) { // 接受 Enum 類型
        if (statusEnum == null) return "未知狀態";
        String statusCode = statusEnum.name(); // 獲取 ENUM 的名稱 (例如 "A", "L", "R")
        switch (statusCode) {
            case "A": return "BCM在館可借閱";
            case "L": return "BCM已借出";
            case "P": return "BCM處理中";
            case "R": return "BCM已預約";
            case "U": return "BCM已下架";
            default: return "BCM未知狀態";
        }
    }
}