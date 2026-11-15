package com.library.dao;
import com.library.util.DB;
import java.sql.*;
public class AuthDAO {
 public boolean login(String username,String password) throws Exception {
  String sql="SELECT id FROM librarians WHERE username=? AND password=?";
  try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
   p.setString(1,username);p.setString(2,password);
   try(ResultSet r=p.executeQuery()){return r.next();}
  }
 }
}
