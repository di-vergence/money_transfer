package com.money.transfer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AccountDaoJDBC {

  public void addMoney(int id, long amount) {
    try (Connection conn = DriverManager.getConnection("jdbc:h2:./test",
        "sa", "");) {

      PreparedStatement st1 = null;

      st1 = conn.prepareStatement("UPDATE  INTO account values(?,?);");
      st1.setInt(1, id);
      st1.setLong(2, amount);
      st1.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public long getAmount(int id) {
    try (Connection conn = DriverManager.getConnection("jdbc:h2:./test",
        "sa", "");) {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery("SELECT amount FROM account;");

      rs.next();
      return rs.getLong("amount");
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public void createAccount() {
    try (Connection conn = DriverManager.getConnection("jdbc:h2:./test",
        "sa", "");) {
      Statement st = conn.createStatement();
      st.executeUpdate("INSERT INTO ACCOUNT values(1, 0);");
      st.executeUpdate("INSERT INTO ACCOUNT values(2, 0);");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
