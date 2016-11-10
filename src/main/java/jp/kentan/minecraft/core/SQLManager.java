package jp.kentan.minecraft.core;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SQLManager {
	final public static SimpleDateFormat FORMATER_SEC = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static String HOST, ID, PASS;
	
	private Connection connection = null;
	private boolean isConnecting = false;

	public SQLManager() {

		connect();

		if ((connection) != null) {
			close();
		} else {
			NekoCore.LOG.warning("connection failure.");
		}
	}

	private void connect() {		
		if (connection != null || isConnecting) {
			NekoCore.LOG.warning("already connecting...");
			return;
		}

		try {
			isConnecting = true;

			Class.forName("com.mysql.jdbc.Driver").newInstance();

			connection = DriverManager.getConnection(HOST, ID, PASS);
			NekoCore.LOG.info("connection established.");

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			NekoCore.LOG.warning("JDBCドライバのロードに失敗しました.");
		} catch (SQLException e) {
			NekoCore.LOG.warning(e.getMessage());
		}
	}

	public void close() {
		try {
			connection.close();
			connection = null;
			NekoCore.LOG.info("connection closed.");
		} catch (Exception e) {
			NekoCore.LOG.warning("close: " + e.getMessage());
		} finally {
			isConnecting = false;
		}
	}

	public ResultSet query(String query) {
		ResultSet result = null;
		Statement stm = null;

		connect();

		try {
			stm = connection.createStatement();
			result = stm.executeQuery(query);
		} catch (SQLException e) {
			NekoCore.LOG.warning("SQLｴﾗｰ:" + e.getMessage());
		} catch (Exception e){
			NekoCore.LOG.warning(e.getMessage());
		}

		return result;
	}

	public void update(String update) {		
		Calendar now = Calendar.getInstance();
		connect();
		try {
			String date = FORMATER_SEC.format(now.getTime());

			update = update.replace("{date}", "STR_TO_DATE('" + date + "','%Y/%m/%d %H:%i:%s')");

			Statement stm = connection.createStatement();
			NekoCore.LOG.info("update: " + stm.executeUpdate(update) + ".");
		} catch (Exception e) {
			NekoCore.LOG.warning(e.getMessage() + "(" + update + ")");
		}
	}
}
