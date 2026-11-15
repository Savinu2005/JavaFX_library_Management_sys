package com.library.model;
public class Book {
 public final int id, year, quantity, available;
 public final String isbn,title,author,category;
 public Book(int id,String isbn,String title,String author,String category,int year,int quantity,int available){
  this.id=id;this.isbn=isbn;this.title=title;this.author=author;this.category=category;
  this.year=year;this.quantity=quantity;this.available=available;
 }
 @Override public String toString(){return title+" — "+isbn+" (available: "+available+")";}
}
