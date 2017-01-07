package com.model.db.db.db.store.generated;

import com.model.db.db.db.business_user.BusinessUser;
import com.model.db.db.db.location.Location;
import com.model.db.db.db.store.Store;
import com.speedment.runtime.core.manager.Manager;
import com.speedment.runtime.core.util.OptionalUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import javax.annotation.Generated;

/**
 * The generated base implementation of the {@link
 * com.model.db.db.db.store.Store}-interface.
 * <p>
 * This file has been automatically generated by Speedment. Any changes made to
 * it will be overwritten.
 * 
 * @author Speedment
 */
@Generated("Speedment")
public abstract class GeneratedStoreImpl implements Store {
    
    private int id;
    private String city;
    private String name;
    private int businessUserId;
    private int locationId;
    private String logo;
    
    protected GeneratedStoreImpl() {
        
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public Optional<String> getCity() {
        return Optional.ofNullable(city);
    }
    
    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
    
    @Override
    public int getBusinessUserId() {
        return businessUserId;
    }
    
    @Override
    public int getLocationId() {
        return locationId;
    }
    
    @Override
    public Optional<String> getLogo() {
        return Optional.ofNullable(logo);
    }
    
    @Override
    public Store setId(int id) {
        this.id = id;
        return this;
    }
    
    @Override
    public Store setCity(String city) {
        this.city = city;
        return this;
    }
    
    @Override
    public Store setName(String name) {
        this.name = name;
        return this;
    }
    
    @Override
    public Store setBusinessUserId(int businessUserId) {
        this.businessUserId = businessUserId;
        return this;
    }
    
    @Override
    public Store setLocationId(int locationId) {
        this.locationId = locationId;
        return this;
    }
    
    @Override
    public Store setLogo(String logo) {
        this.logo = logo;
        return this;
    }
    
    @Override
    public BusinessUser findBusinessUserId(Manager<BusinessUser> foreignManager) {
        return foreignManager.stream().filter(BusinessUser.ID.equal(getBusinessUserId())).findAny().orElse(null);
    }
    
    @Override
    public Location findLocationId(Manager<Location> foreignManager) {
        return foreignManager.stream().filter(Location.ID.equal(getLocationId())).findAny().orElse(null);
    }
    
    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", "{ ", " }");
        sj.add("id = " + Objects.toString(getId()));
        sj.add("city = " + Objects.toString(OptionalUtil.unwrap(getCity())));
        sj.add("name = " + Objects.toString(OptionalUtil.unwrap(getName())));
        sj.add("businessUserId = " + Objects.toString(getBusinessUserId()));
        sj.add("locationId = " + Objects.toString(getLocationId()));
        sj.add("logo = " + Objects.toString(OptionalUtil.unwrap(getLogo())));
        return "StoreImpl " + sj.toString();
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) { return true; }
        if (!(that instanceof Store)) { return false; }
        final Store thatStore = (Store)that;
        if (this.getId() != thatStore.getId()) {return false; }
        if (!Objects.equals(this.getCity(), thatStore.getCity())) {return false; }
        if (!Objects.equals(this.getName(), thatStore.getName())) {return false; }
        if (this.getBusinessUserId() != thatStore.getBusinessUserId()) {return false; }
        if (this.getLocationId() != thatStore.getLocationId()) {return false; }
        if (!Objects.equals(this.getLogo(), thatStore.getLogo())) {return false; }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Integer.hashCode(getId());
        hash = 31 * hash + Objects.hashCode(getCity());
        hash = 31 * hash + Objects.hashCode(getName());
        hash = 31 * hash + Integer.hashCode(getBusinessUserId());
        hash = 31 * hash + Integer.hashCode(getLocationId());
        hash = 31 * hash + Objects.hashCode(getLogo());
        return hash;
    }
}