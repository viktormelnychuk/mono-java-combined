package com.viktor.mono.mapper;


import com.viktor.mono.dto.UserDto;
import com.viktor.mono.dto.monobank.MonoAccountDto;
import com.viktor.mono.entity.MonoAccount;
import com.viktor.mono.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
@Service
public interface MonoAccountMapper {
    MonoAccountMapper INSTANCE = Mappers.getMapper(MonoAccountMapper.class);

    MonoAccountDto fromEntity(MonoAccount account);

    @Mappings({@Mapping(source = "id", target = "monoAccountId"), @Mapping(target = "id", ignore = true)})
    MonoAccount fromDto(MonoAccountDto dto);
}
