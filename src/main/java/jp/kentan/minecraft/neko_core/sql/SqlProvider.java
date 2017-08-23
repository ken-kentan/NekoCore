package jp.kentan.minecraft.neko_core.sql;

import jp.kentan.minecraft.neko_core.utils.Log;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SqlProvider {

    private final static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private Config mConfig;

    private Connection mConnection;

    public SqlProvider(Config config){
        mConfig = config;

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            Log.warn("Failed to load JDBC driver.");
        }

        mConnection = connect();
    }

    private Connection connect() {
        if (mConnection != null) {
            return mConnection;
        }

        try {
            return DriverManager.getConnection(mConfig.mHost, mConfig.mId, mConfig.mPassword);
        } catch (SQLException e) {
            Log.warn(e.getMessage());
        }

        return null;
    }

    public void close() {
        if(mConnection == null){
            return;
        }

        try {
            mConnection.close();
            mConnection = null;
        } catch (Exception e) {
            Log.warn("failed to close connection: " + e.getMessage());
        }
    }

    public ResultSet query(String query) {
        mConnection = connect();

        try {
            assert mConnection != null;
            Statement statement = mConnection.createStatement();

            return statement.executeQuery(query);
        } catch (SQLException e) {
            Log.warn("failed to sql query:" + e.getMessage());
        } catch (Exception e){
            Log.warn(e.getMessage());
        }

        return null;
    }

    public void update(String update) {
        mConnection = connect();

        try {
            String date = FORMAT.format(new Date());

            update = update.replace("{date}", "STR_TO_DATE('" + date + "','%Y/%m/%d %H:%i:%s')");

            Statement statement = mConnection.createStatement();
            statement.executeUpdate(update);
        } catch (Exception e) {
            Log.warn(e.getMessage() + "(" + update + ")");
        }
    }


    public static class Config{

        String mHost, mId, mPassword;

        public Config(String host, String id, String password){
            mHost = host;
            mId = id;
            mPassword = password;
        }
    }
}
