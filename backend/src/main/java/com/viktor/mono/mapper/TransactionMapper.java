package com.viktor.mono.mapper;


import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.monobank.MonoTransactionDto;
import com.viktor.mono.dto.monobank.StatementItemDto;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.service.MccService;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Mapper(componentModel = "spring", uses = {MonoAccountMapper.class})
@Service
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    TransactionDto fromEntity(Transaction transaction);

    Transaction fromDto(TransactionDto dto);

    @Mappings({
            @Mapping(source = "id", target = "monoId"),
            @Mapping(source = "time", target = "time", qualifiedByName = "longToDateTime"),
            @Mapping(target = "mccDescription", expression = "java(mccService.mccCodeToDescription(monoTransactionDto.getMcc()))"),
            @Mapping(target = "id", ignore = true)
    })
    TransactionDto fromMonoTransaction(MonoTransactionDto monoTransactionDto, @Context MccService mccService);

    @Mappings({
            @Mapping(source = "id", target = "monoId"),
            @Mapping(source = "time", target = "time", qualifiedByName = "longToDateTime"),
            @Mapping(target = "mccDescription", expression = "java(mccService.mccCodeToDescription(dto.getMcc()))"),
            @Mapping(target = "id", ignore = true)
    })
    TransactionDto fromStatementItem(StatementItemDto dto, @Context MccService mccService);

    @Named("longToDateTime")
    static LocalDateTime longToDateTime(Long value) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(value), TimeZone.getTimeZone("Europe/Kyiv").toZoneId());
    }
}
