package org.example.mapper;

import org.example.dto.WalletBalanceResponseDto;
import org.example.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface WalletMapper {

    WalletBalanceResponseDto toOutputDto(Wallet wallet);
}
