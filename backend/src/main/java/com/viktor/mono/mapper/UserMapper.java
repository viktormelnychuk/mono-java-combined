package com.viktor.mono.mapper;


import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.UserDto;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring", uses = {MonoAccountMapper.class})
@Service
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto fromEntity(User transaction);

    User fromDto(UserDto dto);
}
