package com.library.model;
public class Member {
 public final int id; public final String code,name,email,phone,address;
 public Member(int id,String code,String name,String email,String phone,String address){
  this.id=id;this.code=code;this.name=name;this.email=email;this.phone=phone;this.address=address;
 }
 @Override public String toString(){return name+" ("+code+")";}
}
