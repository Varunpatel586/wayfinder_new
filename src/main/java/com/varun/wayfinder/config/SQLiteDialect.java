package com.varun.wayfinder.config;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.sequence.NoSequenceSupport;
import org.hibernate.dialect.sequence.SequenceSupport;

/**
 * Custom SQLite dialect to prevent duplicate primary key generation.
 */
public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super(DatabaseVersion.make(3));
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        // Disable Hibernate's automatic PK constraint addition
        return "";
    }

    public boolean supportsUniqueConstraintInCreateAlterTable() {
        return false;
    }

    @Override
    public SequenceSupport getSequenceSupport() {
        return NoSequenceSupport.INSTANCE;
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    /**
     * Custom identity column support for SQLite
     */
    private static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {

        @Override
        public String getIdentityColumnString(int type) {
            // SQLite uses "integer primary key autoincrement"
            return "integer primary key autoincrement";
        }

        @Override
        public boolean supportsInsertSelectIdentity() {
            return false;
        }

        @Override
        public boolean hasDataTypeInIdentityColumn() {
            // SQLite does not require explicit data type in identity column
            return false;
        }
    }
}
