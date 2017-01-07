package com.model.db.db.db.store.generated;

import com.model.db.db.db.store.Store;
import com.model.db.db.db.store.StoreImpl;
import com.speedment.common.injector.annotation.ExecuteBefore;
import com.speedment.runtime.config.identifier.TableIdentifier;
import com.speedment.runtime.core.component.sql.SqlPersistenceComponent;
import com.speedment.runtime.core.component.sql.SqlStreamSupplierComponent;
import com.speedment.runtime.core.exception.SpeedmentException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Generated;
import static com.speedment.common.injector.State.RESOLVED;
import static com.speedment.runtime.core.internal.util.sql.ResultSetUtil.*;

/**
 * The generated Sql Adapter for a {@link com.model.db.db.db.store.Store}
 * entity.
 * <p>
 * This file has been automatically generated by Speedment. Any changes made to
 * it will be overwritten.
 * 
 * @author Speedment
 */
@Generated("Speedment")
public abstract class GeneratedStoreSqlAdapter {
    
    private final TableIdentifier<Store> tableIdentifier;
    
    protected GeneratedStoreSqlAdapter() {
        this.tableIdentifier = TableIdentifier.of("db", "db", "store");
    }
    
    @ExecuteBefore(RESOLVED)
    void installMethodName(SqlStreamSupplierComponent streamSupplierComponent, SqlPersistenceComponent persistenceComponent) {
        streamSupplierComponent.install(tableIdentifier, this::apply);
        persistenceComponent.install(tableIdentifier);
    }
    
    protected Store apply(ResultSet resultSet) throws SpeedmentException{
        final Store entity = createEntity();
        try {
            entity.setId(resultSet.getInt(1));
            entity.setCity(getString(resultSet, 2));
            entity.setName(getString(resultSet, 3));
            entity.setBusinessUserId(resultSet.getInt(4));
            entity.setLocationId(resultSet.getInt(5));
            entity.setLogo(getString(resultSet, 6));
        } catch (final SQLException sqle) {
            throw new SpeedmentException(sqle);
        }
        return entity;
    }
    
    protected StoreImpl createEntity() {
        return new StoreImpl();
    }
}