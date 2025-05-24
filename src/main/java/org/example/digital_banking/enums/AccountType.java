package org.example.digital_banking.enums;

/**
 * Enum representing the types of bank accounts in the system.
 */
public enum AccountType {
    /**
     * Savings account type
     */
    SAVINGS("SAV"),
    
    /**
     * Current account type
     */
    CURRENT("CUR");
    
    private final String code;
    
    /**
     * Constructor for AccountType enum
     * 
     * @param code The string code representing the account type
     */
    AccountType(String code) {
        this.code = code;
    }
    
    /**
     * Get the string code for this account type
     * 
     * @return The string code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get an AccountType from its string code
     * 
     * @param code The string code
     * @return The corresponding AccountType or null if not found
     */
    public static AccountType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (AccountType type : AccountType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}