package org.example.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.example.model.Wallet;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUuid(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "javax.persistence.lock.timeout", value = "1000"))
    @Query("SELECT w FROM Wallet w WHERE w.uuid = :uuid")
    Optional<Wallet> findByUuidWithLock(@Param("uuid") UUID uuid);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.uuid = :uuid")
    void depositBalance(@Param("amount") BigDecimal amount, @Param("uuid") UUID uuid);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount WHERE w.uuid = :uuid")
    void withdrawBalance(@Param("amount") BigDecimal amount, @Param("uuid") UUID uuid);
}
