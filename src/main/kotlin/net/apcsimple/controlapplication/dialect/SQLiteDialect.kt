package net.apcsimple.controlapplication.dialect

import org.hibernate.dialect.Dialect
import org.hibernate.dialect.function.SQLFunctionTemplate
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.dialect.function.VarArgsSQLFunction
import org.hibernate.type.IntegerType
import org.hibernate.type.StringType
import java.sql.Types

class SQLiteDialect: Dialect {

    constructor() {
        registerColumnType(Types.BIT, "integer")
        registerColumnType(Types.TINYINT, "tinyint")
        registerColumnType(Types.SMALLINT, "smallint")
        registerColumnType(Types.INTEGER, "integer")
        registerColumnType(Types.BIGINT, "bigint")
        registerColumnType(Types.FLOAT, "float")
        registerColumnType(Types.REAL, "real")
        registerColumnType(Types.DOUBLE, "double")
        registerColumnType(Types.NUMERIC, "numeric")
        registerColumnType(Types.DECIMAL, "decimal")
        registerColumnType(Types.CHAR, "char")
        registerColumnType(Types.VARCHAR, "varchar")
        registerColumnType(Types.LONGVARCHAR, "longvarchar")
        registerColumnType(Types.DATE, "date")
        registerColumnType(Types.TIME, "time")
        registerColumnType(Types.TIMESTAMP, "timestamp")
        registerColumnType(Types.BINARY, "blob")
        registerColumnType(Types.VARBINARY, "blob")
        registerColumnType(Types.LONGVARBINARY, "blob")
        // registerColumnType(Types.NULL, "null");
        registerColumnType(Types.BLOB, "blob")
        registerColumnType(Types.CLOB, "clob")
        registerColumnType(Types.BOOLEAN, "integer")
        registerFunction(
            "concat", VarArgsSQLFunction(
                StringType.INSTANCE, "",
                "||", ""
            )
        )
        registerFunction(
            "mod", SQLFunctionTemplate(
                IntegerType.INSTANCE,
                "?1 % ?2"
            )
        )
        registerFunction(
            "substr", StandardSQLFunction(
                "substr",
                StringType.INSTANCE
            )
        )
        registerFunction(
            "substring", StandardSQLFunction(
                "substr",
                StringType.INSTANCE
            )
        )
    }

    fun supportsIdentityColumns(): Boolean {
        return true
    }

    /*
     * public boolean supportsInsertSelectIdentity() { return true; // As
     * specify in NHibernate dialect }
     */

    /*
     * public boolean supportsInsertSelectIdentity() { return true; // As
     * specify in NHibernate dialect }
     */
    fun hasDataTypeInIdentityColumn(): Boolean {
        return false // As specify in NHibernate dialect
    }

    /*
     * public String appendIdentitySelectToInsert(String insertString) { return
     * new StringBuffer(insertString.length()+30). // As specify in NHibernate
     * dialect append(insertString).
     * append("; ").append(getIdentitySelectString()). toString(); }
     */

    /*
     * public String appendIdentitySelectToInsert(String insertString) { return
     * new StringBuffer(insertString.length()+30). // As specify in NHibernate
     * dialect append(insertString).
     * append("; ").append(getIdentitySelectString()). toString(); }
     */
    fun getIdentityColumnString(): String? {
        // return "integer primary key autoincrement";
        return "integer"
    }

    fun getIdentitySelectString(): String? {
        return "select last_insert_rowid()"
    }

    //    public boolean supportsLimit() {
    //        return true;
    //    }
    //
    //    protected String getLimitString(String query, boolean hasOffset) {
    //        return new StringBuffer(query.length() + 20).append(query)
    //                .append(hasOffset ? " limit ? offset ?" : " limit ?")
    //                .toString();
    //    }

    //    public boolean supportsLimit() {
    //        return true;
    //    }
    //
    //    protected String getLimitString(String query, boolean hasOffset) {
    //        return new StringBuffer(query.length() + 20).append(query)
    //                .append(hasOffset ? " limit ? offset ?" : " limit ?")
    //                .toString();
    //    }
    fun supportsTemporaryTables(): Boolean {
        return true
    }

    fun getCreateTemporaryTableString(): String? {
        return "create temporary table if not exists"
    }

    fun dropTemporaryTableAfterUse(): Boolean {
        return false
    }

    override fun supportsCurrentTimestampSelection(): Boolean {
        return true
    }

    override fun isCurrentTimestampSelectStringCallable(): Boolean {
        return false
    }

    override fun getCurrentTimestampSelectString(): String? {
        return "select current_timestamp"
    }

    override fun supportsUnionAll(): Boolean {
        return true
    }

    override fun hasAlterTable(): Boolean {
        return false // As specify in NHibernate dialect
    }

    override fun dropConstraints(): Boolean {
        return false
    }

    override fun getAddColumnString(): String? {
        return "add column"
    }

    override fun getForUpdateString(): String? {
        return ""
    }

    override fun supportsOuterJoinForUpdate(): Boolean {
        return false
    }

    override fun getDropForeignKeyString(): String? {
        throw UnsupportedOperationException(
            "No drop foreign key syntax supported by SQLiteDialect"
        )
    }

    override fun getAddForeignKeyConstraintString(
        constraintName: String?,
        foreignKey: Array<String?>?, referencedTable: String?, primaryKey: Array<String?>?,
        referencesPrimaryKey: Boolean
    ): String? {
        throw UnsupportedOperationException(
            "No add foreign key syntax supported by SQLiteDialect"
        )
    }

    override fun getAddPrimaryKeyConstraintString(constraintName: String?): String? {
        throw UnsupportedOperationException(
            "No add primary key syntax supported by SQLiteDialect"
        )
    }

    override fun supportsIfExistsBeforeTableName(): Boolean {
        return true
    }

    override fun supportsCascadeDelete(): Boolean {
        return false
    }
}