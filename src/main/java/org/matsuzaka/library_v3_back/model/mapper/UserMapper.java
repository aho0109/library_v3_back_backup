package org.matsuzaka.library_v3_back.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto;
import org.matsuzaka.library_v3_back.model.entity.User;

@Mapper(componentModel = "spring") // componentModel = "spring" 讓 Spring 自動管理這個 Mapper
public interface UserMapper {

    // 獲取 Mapper 實例 (如果不用 Spring 自動注入，可以用這個)
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * 將 User 實體及其關聯的 UserDetail 映射到 UserDetailRespDto。
     * - user.id 會自動映射到 UserDetailRespDto.id (如果名稱和型別匹配)。
     * - user.account 會自動映射到 UserDetailRespDto.account。
     * - UserDetail 中的其他欄位需要明確映射。
     */
    @Mapping(source = "userDetail.name", target = "name")
    @Mapping(source = "userDetail.email", target = "email")
    @Mapping(source = "userDetail.phone", target = "phone")
    @Mapping(source = "userDetail.address", target = "address")
    @Mapping(source = "cardId", target = "cardId")
    // id, account 會被隱式映射 (如果名稱和型別匹配)
    UserDetailRespDto toUserDetailRespDto(User user);
}
