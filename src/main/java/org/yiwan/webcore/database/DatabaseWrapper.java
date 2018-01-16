package org.yiwan.webcore.database;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.SoftAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yiwan.webcore.test.TestCaseManager;
import org.yiwan.webcore.test.pojo.DatabaseServer;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kent gu on 6/22/2017.
 */
public class DatabaseWrapper implements IDatabaseWrapper {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private String dbUrl;
	private String dbDriver;
	private Connection connection = null;
	private boolean isConnected = false;
	private Map<String, ResultSet> resultMap = new HashMap<String, ResultSet>();

	public DatabaseWrapper initDatabaseWrapper (DatabaseServer databaseServer) {
		dbDriver = databaseServer.getDriver();
		setDbUrl(databaseServer);
		return this;
	}

	public SoftAssertions getSoftAssertions() {
		return TestCaseManager.getTestCase().getSoftAssertions();
	}

	public Connection connect() throws Exception {
		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(getDbUrl());
			if (connection != null) {
				logger.info("Connected to " + getDbUrl());
				isConnected = true;
			}
		} catch (ClassNotFoundException cnfe) {
			logger.error(dbDriver + " not found");
			cnfe.printStackTrace();
		} catch (SQLException se) {
			logger.error("failed to connect " + getDbUrl());
			se.printStackTrace();
			throw new Exception();
		}

		return connection;
	}

	public ResultSet query(String sql) throws Exception {
		ResultSet result = null;
		try{
			PreparedStatement statement = (PreparedStatement) connection.prepareStatement(sql);
			result = statement.executeQuery();
		} catch (SQLException se) {
			logger.error("failed to execute: " + sql);
			se.printStackTrace();
			closeConnection();
			throw new Exception();
		}
		return result;
	}

	public void addResultSetToMap(String key, ResultSet result) throws Exception {
		resultMap.put(key, result);
	}

	public List<HashMap<String, String>> getResult(String key) throws Exception {
		ResultSet resultSet = resultMap.get(key);
		resultMap.remove(key);

		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		ResultSetMetaData data = resultSet.getMetaData();
		int columns = data.getColumnCount();
		while (resultSet.next()) {
			HashMap row = new HashMap();
			for (int i = 1; i <= columns ; i++) {
				row.put(data.getColumnName(i), String.valueOf(resultSet.getObject(i)));
			}
			list.add(row);
		}
//		ResultSetMetaData data = resultSet.getMetaData();
//		int columns = data.getColumnCount();
//		RowSetDynaClass rows = new RowSetDynaClass(resultSet);
//		List<DynaBean> resultList = rows.getRows();
//		for (DynaBean dynaBean : resultList) {
//			HashMap row = new HashMap();
//			for (int i = 1; i < columns; i++) {
//				row.put(data.getColumnName(i), String.valueOf(dynaBean.get(data.getColumnName(i).toLowerCase())));
//			}
//			list.add(row);
//		}
		return list;
	}

	public void closeConnection() throws Exception {
		try {
			if (isConnected)
				connection.close();
			isConnected = false;
			logger.info("Connection " + getDbUrl() + " is closed");
		} catch (SQLException se) {
			logger.error("failed to close connection " + getDbUrl());
			se.printStackTrace();
			throw new Exception();
		}

	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(DatabaseServer databaseServer) {
		if (databaseServer.getDriver().contains("OracleDriver")) {
			if (databaseServer.getUsername() != null
					&& databaseServer.getPassword() != null
					&& databaseServer.getHost() != null
					&& databaseServer.getPort() != null
					&& databaseServer.getInstance() != null)
			this.dbUrl = "jdbc:oracle:thin:" + databaseServer.getUsername() + "/" + databaseServer.getPassword() + "@"
					+ databaseServer.getHost() + ":" + databaseServer.getPort() + ":" + databaseServer.getInstance();
		}
	}
	
	public void setDbUrl(String url) {
		this.dbUrl = url;
	}

	public String getDBDriver() {
		return dbDriver;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public FluentDatabaseAssertion assertThat(Map<String, String> map) {
		return new FluentDatabaseAssertion(map);
	}

	public FluentDatabaseValidation validateThat(Map<String, String> map) {
		return new FluentDatabaseValidation(map);
	}

	private class FluentDatabaseAssertion implements IFluentDatabaseAssertion{
		private Map<String, String> map;

		FluentDatabaseAssertion (Map<String, String> map) {
			this.map = map;
		}

		@Override
		public AbstractCharSequenceAssert<?, String> columnValue(String key) {
			return org.assertj.core.api.Assertions.assertThat(map.get(key)).as("assert Column %s value", map.get(key));
		}

		@Override
		public AbstractBooleanAssert<?> rowExisted(List<HashMap<String, String>> actual) {
			boolean flag = false;
			for (Map <String, String> map : actual) {
				int i = 0;
				for (String key : this.map.keySet()) {
					try {
						if (!map.get(key).equals(this.map.get(key)))
							break;
						i++;
					}catch (NullPointerException npe) {
						break;
					}
				}
				if (i == this.map.size()) {
					flag = true;
					break;
				}
			}
			return org.assertj.core.api.Assertions.assertThat(flag).as("assert %s in the %s", this.map, actual);
		}

		@Override
		public AbstractBooleanAssert<?> rowDistinctExisted(List<HashMap<String, String>> actual) {
			return org.assertj.core.api.Assertions.assertThat(actual.contains(this.map)).as("assert %s in the %s", this.map, actual);
		}
	}

	private class FluentDatabaseValidation implements IFluentDatabaseAssertion {
		private Map<String, String> map;

		FluentDatabaseValidation (Map<String, String> map) {
			this.map = map;
		}

		@Override
		public AbstractCharSequenceAssert<?, String> columnValue(String key) {
			return getSoftAssertions().assertThat(map.get(key)).as("validate Column %s value", map.get(key));
		}

		@Override
		public AbstractBooleanAssert<?> rowExisted(List<HashMap<String, String>> actual) {
			boolean flag = false;
			for (Map <String, String> map : actual) {
				int i = 0;
				for (String key : this.map.keySet()) {
					try {
						if (!map.get(key).equals(this.map.get(key)))
							break;
						i++;
					}catch (NullPointerException npe) {
						break;
					}
				}
				if (i == this.map.size()) {
					flag = true;
					break;
				}
			}
			return getSoftAssertions().assertThat(flag).as("validate %s in the %s", this.map, actual);
		}

		@Override
		public AbstractBooleanAssert<?> rowDistinctExisted(List<HashMap<String, String>> actual) {
			return getSoftAssertions().assertThat(actual.contains(this.map)).as("validate %s in the %s", this.map, actual);
		}
	}

}
