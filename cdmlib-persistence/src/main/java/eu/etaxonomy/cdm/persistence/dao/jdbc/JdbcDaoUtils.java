// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author ayco_holleman
 * @date 20 jan. 2015
 *
 */
public class JdbcDaoUtils {

    private static final Logger logger = Logger.getLogger(JdbcDaoUtils.class);

    /**
     * Closes a JDBC {@code Statement} while trapping and suppressing any
     * {@code SQLException} that may be thrown in the process.
     *
     * @param stmt
     *            The {@code Statement} to close
     */
    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Throwable t) {
                logger.error("Error closing JDBC Statement", t);
            }
        }
    }

    public static void startTransaction(DataSource datasource) {
        try {
            if (datasource instanceof ICdmDataSource) {
                ((ICdmDataSource) datasource).startTransaction();
            } else {
                datasource.getConnection().setAutoCommit(false);
            }
        } catch (Throwable t) {
            logger.error("Error while starting transaction", t);
        }
    }

    /**
     *
     * @param datasource
     */
    public static void commit(DataSource datasource) {
        try {
            if (datasource instanceof ICdmDataSource) {
                ((ICdmDataSource) datasource).commitTransaction();
            } else {
                datasource.getConnection().commit();
            }
        } catch (Throwable t) {
            // Also catch NullPointerException when connection == null
            logger.error("Commit failed", t);
        }
    }

    /**
     * Rollback the current transaction while trapping and suppressing any
     * {@code SQLException} that may be thrown in the process.
     *
     * @param connection
     *            The JDBC connection through which the transaction takes place
     */
    public static void rollback(DataSource datasource) {
        try {
            if (datasource instanceof ICdmDataSource) {
                ((ICdmDataSource) datasource).rollback();
            } else if (!datasource.getConnection().getAutoCommit()) {
                datasource.getConnection().rollback();
            }
        } catch (Throwable t) {
            logger.error("Rollback failed", t);
        }
    }

    /**
     * Retrieves the first column from the first record returned by the
     * specified sql query. The column is presumed to be an integer column.
     *
     * @param connection
     * @param sql
     * @return
     * @throws SQLException
     */
    public static int fetchInt(Connection connection, String sql) throws SQLException {
        int result = 0;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } finally {
            close(stmt);
        }
        return result;
    }

}
