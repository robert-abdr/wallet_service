import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.example.controller.handler.ErrorHandler;
import org.example.controller.wallet.WalletController;
import org.example.dto.WalletBalanceResponseDto;
import org.example.dto.WalletRequestPostDto;
import org.example.exception.NotEnoughMoneyException;
import org.example.exception.WalletNoExistException;
import org.example.exception.WalletOperationLockException;
import org.example.model.OperationType;
import org.example.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID testUuid = UUID.fromString("113e4467-e80b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        ErrorHandler handler = new ErrorHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(walletController)
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void processWalletOperationDepositValidRequestOk() throws Exception {
        WalletRequestPostDto dto = WalletRequestPostDto.builder()
                .walletUuid(testUuid)
                .operationType(OperationType.DEPOSIT)
                .amount(new BigDecimal("100.50"))
                .build();

        doNothing().when(walletService).processWalletOperation(any(WalletRequestPostDto.class));

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Operation with wallet success"));

        verify(walletService, times(1)).processWalletOperation(any(WalletRequestPostDto.class));
    }

    @Test
    void processWalletOperationWithdrawValidRequestOk() throws Exception {
        WalletRequestPostDto dto = WalletRequestPostDto.builder()
                .walletUuid(testUuid)
                .operationType(OperationType.WITHDRAW)
                .amount(new BigDecimal("50.00"))
                .build();

        doNothing().when(walletService).processWalletOperation(any(WalletRequestPostDto.class));

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Operation with wallet success"));
    }

    @Test
    void processWalletOperationWalletNotFoundNegative() throws Exception {
        WalletRequestPostDto dto = WalletRequestPostDto.builder()
                .walletUuid(testUuid)
                .operationType(OperationType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .build();

        doThrow(new WalletNoExistException("Wallet not found"))
                .when(walletService)
                .processWalletOperation(any(WalletRequestPostDto.class));

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void processWalletOperationNotEnoughMoneyNegative() throws Exception {
        WalletRequestPostDto dto = WalletRequestPostDto.builder()
                .walletUuid(testUuid)
                .operationType(OperationType.WITHDRAW)
                .amount(new BigDecimal("1000.00"))
                .build();

        doThrow(new NotEnoughMoneyException("Not enough money"))
                .when(walletService)
                .processWalletOperation(any(WalletRequestPostDto.class));

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    void processWalletOperationWalletLockedNegative() throws Exception {
        WalletRequestPostDto dto = WalletRequestPostDto.builder()
                .walletUuid(testUuid)
                .operationType(OperationType.WITHDRAW)
                .amount(new BigDecimal("100.00"))
                .build();

        doThrow(new WalletOperationLockException("Wallet locked"))
                .when(walletService)
                .processWalletOperation(any(WalletRequestPostDto.class));

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void processWalletOperationInvalidJsonNegative() throws Exception {
        String invalidJson = "{\"invalid\": \"data\"}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processWalletOperationMissingWalletUuidNegative() throws Exception {
        String jsonMissingUuid = "{\"operationType\": \"DEPOSIT\", \"amount\": 100.00}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMissingUuid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processWalletOperationInvalidAmountTooSmallNegative() throws Exception {

        String jsonInvalidAmount = "{\"walletUuid\": \"123e4567-e89b-12d3-a456-426614174000\","
                + " \"operationType\": \"DEPOSIT\", \"amount\": 0.00}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidAmount))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processWalletOperationInvalidAmountTooLargeNegative() throws Exception {
        String jsonInvalidAmount = "{\"walletUuid\": \"123e4567-e89b-12d3-a456-426614174000\","
                + " \"operationType\": \"DEPOSIT\", \"amount\": 1000001.00}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidAmount))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processWalletOperationInvalidOperationTypeNegative() throws Exception {
        String jsonInvalidOperation = "{\"walletUuid\": \"123e4567-e89b-12d3-a456-426614174000\","
                + " \"operationType\": \"INVALID\", \"amount\": 100.00}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidOperation))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBalanceValidUuidOk() throws Exception {
        WalletBalanceResponseDto responseDto = new WalletBalanceResponseDto(new BigDecimal("1000.50"));

        when(walletService.getBalance(testUuid)).thenReturn(responseDto);

        mockMvc.perform(get("/v1/wallets/{walletUuid}", testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.50));
    }

    @Test
    void getBalanceNonExistentWalletNegative() throws Exception {
        when(walletService.getBalance(testUuid)).thenThrow(new WalletNoExistException("Wallet not found"));

        mockMvc.perform(get("/v1/wallets/{walletUuid}", testUuid)).andExpect(status().isNotFound());
    }

    @Test
    void getBalanceInvalidUuidFormatNegative() throws Exception {
        mockMvc.perform(get("/v1/wallets/{walletUuid}", "invalid-uuid-format")).andExpect(status().isBadRequest());
    }

    @Test
    void processWalletOperationAmountWithManyDecimalsNegative() throws Exception {
        String jsonManyDecimals = "{\"walletUuid\": \"123e4567-e89b-12d3-a456-426614174000\","
                + " \"operationType\": \"DEPOSIT\", \"amount\": 100.123}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonManyDecimals))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processWalletOperationNullAmountNegative() throws Exception {
        String jsonNullAmount = "{\"walletUuid\": \"123e4567-e89b-12d3-a456-426614174000\","
                + " \"operationType\": \"DEPOSIT\", \"amount\": null}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNullAmount))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processWalletOperationNullOperationTypeNegative() throws Exception {
        String jsonNullOperation = "{\"walletUuid\": \"123e4567-e89b-12d3-a456-426614174000\","
                + " \"operationType\": null, \"amount\": 100.00}";

        mockMvc.perform(post("/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNullOperation))
                .andExpect(status().isBadRequest());
    }
}
