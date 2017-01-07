package com.model.db.db.db.business_user.generated;

import com.model.db.db.db.business_user.BusinessUser;
import com.speedment.runtime.core.util.OptionalUtil;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import javax.annotation.Generated;

/**
 * The generated base implementation of the {@link
 * com.model.db.db.db.business_user.BusinessUser}-interface.
 * <p>
 * This file has been automatically generated by Speedment. Any changes made to
 * it will be overwritten.
 * 
 * @author Speedment
 */
@Generated("Speedment")
public abstract class GeneratedBusinessUserImpl implements BusinessUser {
    
    private int id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Timestamp createTime;
    private String balance;
    
    protected GeneratedBusinessUserImpl() {
        
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }
    
    @Override
    public Optional<String> getFullName() {
        return Optional.ofNullable(fullName);
    }
    
    @Override
    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }
    
    @Override
    public Optional<Timestamp> getCreateTime() {
        return Optional.ofNullable(createTime);
    }
    
    @Override
    public Optional<String> getBalance() {
        return Optional.ofNullable(balance);
    }
    
    @Override
    public BusinessUser setId(int id) {
        this.id = id;
        return this;
    }
    
    @Override
    public BusinessUser setEmail(String email) {
        this.email = email;
        return this;
    }
    
    @Override
    public BusinessUser setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }
    
    @Override
    public BusinessUser setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
    
    @Override
    public BusinessUser setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
        return this;
    }
    
    @Override
    public BusinessUser setBalance(String balance) {
        this.balance = balance;
        return this;
    }
    
    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", "{ ", " }");
        sj.add("id = " + Objects.toString(getId()));
        sj.add("email = " + Objects.toString(OptionalUtil.unwrap(getEmail())));
        sj.add("fullName = " + Objects.toString(OptionalUtil.unwrap(getFullName())));
        sj.add("phoneNumber = " + Objects.toString(OptionalUtil.unwrap(getPhoneNumber())));
        sj.add("createTime = " + Objects.toString(OptionalUtil.unwrap(getCreateTime())));
        sj.add("balance = " + Objects.toString(OptionalUtil.unwrap(getBalance())));
        return "BusinessUserImpl " + sj.toString();
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) { return true; }
        if (!(that instanceof BusinessUser)) { return false; }
        final BusinessUser thatBusinessUser = (BusinessUser)that;
        if (this.getId() != thatBusinessUser.getId()) {return false; }
        if (!Objects.equals(this.getEmail(), thatBusinessUser.getEmail())) {return false; }
        if (!Objects.equals(this.getFullName(), thatBusinessUser.getFullName())) {return false; }
        if (!Objects.equals(this.getPhoneNumber(), thatBusinessUser.getPhoneNumber())) {return false; }
        if (!Objects.equals(this.getCreateTime(), thatBusinessUser.getCreateTime())) {return false; }
        if (!Objects.equals(this.getBalance(), thatBusinessUser.getBalance())) {return false; }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Integer.hashCode(getId());
        hash = 31 * hash + Objects.hashCode(getEmail());
        hash = 31 * hash + Objects.hashCode(getFullName());
        hash = 31 * hash + Objects.hashCode(getPhoneNumber());
        hash = 31 * hash + Objects.hashCode(getCreateTime());
        hash = 31 * hash + Objects.hashCode(getBalance());
        return hash;
    }
}