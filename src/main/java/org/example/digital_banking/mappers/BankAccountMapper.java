package org.example.digital_banking.mappers;

import org.example.digital_banking.dtos.*;
import org.example.digital_banking.entities.*;
import org.example.digital_banking.enums.AccountStatus;
import org.example.digital_banking.enums.AccountType;
import org.mapstruct.*;

/**
 * Mapper interface for converting between entity objects and DTOs
 */
@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    /**
     * Convert a Customer entity to a CustomerDTO
     *
     * @param customer The Customer entity to convert
     * @return The corresponding CustomerDTO
     */
    CustomerDTO fromCustomer(Customer customer);

    /**
     * Convert a CustomerDTO to a Customer entity
     *
     * @param customerDTO The CustomerDTO to convert
     * @return The corresponding Customer entity
     */
    Customer fromCustomerDTO(CustomerDTO customerDTO);

    /**
     * Convert a SavingAccount entity to a SavingAccountDTO
     *
     * @param savingAccount The SavingAccount entity to convert
     * @return The corresponding SavingAccountDTO
     */
    @Mapping(source = "idBankAccount", target = "id")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(target = "type", constant = "SA")
    @Mapping(source = "customer", target = "customerDTO")
    SavingAccountDTO fromSavingAccount(SavingAccount savingAccount);

    /**
     * Convert a CurrentAccount entity to a CurrentAccountDTO
     *
     * @param currentAccount The CurrentAccount entity to convert
     * @return The corresponding CurrentAccountDTO
     */
    @Mapping(source = "idBankAccount", target = "id")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(target = "type", constant = "CA")
    @Mapping(source = "customer", target = "customerDTO")
    CurrentAccountDTO fromCurrentAccount(CurrentAccount currentAccount);

    /**
     * Convert a BankAccount entity to the appropriate BankAccountDTO subtype
     *
     * @param bankAccount The BankAccount entity to convert
     * @return The corresponding BankAccountDTO (either SavingAccountDTO or CurrentAccountDTO)
     */
    default BankAccountDTO fromBankAccount(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        if (bankAccount instanceof SavingAccount) {
            return fromSavingAccount((SavingAccount) bankAccount);
        } else if (bankAccount instanceof CurrentAccount) {
            return fromCurrentAccount((CurrentAccount) bankAccount);
        } else {
            throw new ClassCastException("Unknown BankAccount type: " + bankAccount.getClass().getName());
        }
    }

    /**
     * Convert an Operation entity to an AccountOperationDTO
     *
     * @param operation The Operation entity to convert
     * @return The corresponding AccountOperationDTO
     */
    AccountOperationDTO fromOperation(Operation operation);

    /**
     * Convert AccountStatus to String
     */
    @Named("statusToString")
    default String statusToString(AccountStatus status) {
        return status != null ? status.toString() : null;
    }
}
